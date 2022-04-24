package com.example.trainingwheel01.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.example.trainingwheel01.R
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.succeeded
import com.example.trainingwheel01.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("app_settings")

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionRequestLauncher: ActivityResultLauncher<Array<out String>>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback


    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLaunchers()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.setupWeather(
            uiState = viewModel.state,
            onGetWeather = viewModel.accept
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                when (destination.id) {
                    R.id.userDetailFragment -> {
                        binding.toolbarTitle.text = getString(R.string.title_user_detail)
                        binding.backButton.isVisible = true
                        binding.backButton.setOnClickListener { onBackPressed() }
                    }
                    R.id.userListFragment   -> {
                        binding.toolbarTitle.text = getString(R.string.title_users)
                        binding.backButton.isVisible = false
                    }
                }
            }
        })

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val lastLocation = locationResult.lastLocation
                val lat = lastLocation.latitude
                val lng = lastLocation.longitude
                val cityName = getCityName(lat, lng)

                lifecycleScope.launch {
                    // Updating the settings will trigger the flow.
                    dataStore.edit { settings ->
                        settings[WEATHER_DATA_AVAILABLE] = true
                        settings[WEATHER_LAT] = lat
                        settings[WEATHER_LNG] = lng
                        settings[WEATHER_CITY_NAME] = cityName
                    }
                }
                binding.updateWeatherInfo(lat, lng, cityName)
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun ActivityMainBinding.setupWeather(
        uiState: StateFlow<UiState>,
        onGetWeather: (UiAction.GetWeather) -> Unit
    ) {

        weatherLayout.weatherPlaceholderLayout.setOnClickListener { handleWeatherLayoutClick() }

        if (checkLocationPermission()) {
            lifecycleScope.launch {
                dataStore.data.collectLatest { value: Preferences ->
                    Timber.tag("Location.Msg").d("Preferences: $value")
                    if (value[WEATHER_DATA_AVAILABLE] == true) {
                        val lat = value[WEATHER_LAT] ?: 0.0
                        val lng = value[WEATHER_LNG] ?: 0.0
                        val cityName = value[WEATHER_CITY_NAME] ?: "Unknown"

                        updateWeatherInfo(lat, lng, cityName)
                        onGetWeather(UiAction.GetWeather(lat.toString(), lng.toString()))
                    } else {
                        // TODO: old data expired
                        getLastKnownLocation()
                    }
                }

                uiState.collectLatest {
                    when {
                        it.weatherData.succeeded -> {
                            val weatherResponse = (it.weatherData as Result.Success).data
                            Timber.tag("Location.Msg").d("Weather response: $weatherResponse")

                            weatherLayout.weatherInfoGroup.isVisible = true
                            weatherLayout.weatherPlaceholderLayout.isVisible = false
                            weatherResponse.main?.temp?.let { temperature ->
                                weatherLayout.weatherTemperature.text =
                                    "$temperature\u00B0"
                            }
                            weatherResponse.weather.firstOrNull()?.let { weather ->
                                weatherLayout.weatherDescription.text =
                                    weather.description
                                val weatherIconCode = weather.icon
                                val weatherIconUrl = "https://openweathermap.org/img/w/$weatherIconCode.png"
                                Glide.with(weatherLayout.weatherIcon)
                                    .load(weatherIconUrl)
                                    .error(R.drawable.cloudy)
                                    .into(weatherLayout.weatherIcon)
                            }
                        }
                    }
                }
            }
        } else {
            weatherLayout.weatherInfoGroup.isVisible = false
            weatherLayout.weatherPlaceholderLayout.isVisible = true
        }
    }

    private fun ActivityMainBinding.updateWeatherInfo(
        lat: Double,
        lng: Double,
        city: String
    ) {
        weatherLayout.weatherPlaceholderLayout.isVisible = false
        weatherLayout.weatherCity.text = city
        lifecycleScope.launch {
            viewModel.getWeatherInfo(lat.toString(), lng.toString()).collectLatest {
                when {
                    it.succeeded -> {
                        val weatherResponse = (it as Result.Success).data
                        Timber.d("Weather response: $weatherResponse")

                        weatherLayout.weatherInfoGroup.isVisible = true
                        weatherLayout.weatherPlaceholderLayout.isVisible = false
                        weatherResponse.main?.temp?.let { temperature ->
                            weatherLayout.weatherTemperature.text =
                                "$temperature\u00B0"
                        }
                        weatherResponse.weather.firstOrNull()?.let { weather ->
                            weatherLayout.weatherDescription.text =
                                weather.description
                            val weatherIconCode = weather.icon
                            val weatherIconUrl = "https://openweathermap.org/img/w/$weatherIconCode.png"
                            Glide.with(weatherLayout.weatherIcon)
                                .load(weatherIconUrl)
                                .error(R.drawable.cloudy)
                                .into(weatherLayout.weatherIcon)
                        }
                    }
                }
            }
        }
    }

    /* Permission already checked */
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    // TODO: change location settings and request updates
                    Timber.tag("Location.Msg").d("No last location!")
                    createLocationRequest()
                } else {
                    Timber.tag("Location.Msg").d("Last location: $location")
                    val lat = location.latitude
                    val lng = location.longitude
                    val cityName = getCityName(lat, lng)

                    lifecycleScope.launch {
                        // Updating the settings will trigger the flow.
                        dataStore.edit { settings ->
                            settings[WEATHER_DATA_AVAILABLE] = true
                            settings[WEATHER_LAT] = lat
                            settings[WEATHER_LNG] = lng
                            settings[WEATHER_CITY_NAME] = cityName
                        }

                        binding.updateWeatherInfo(lat, lng, cityName)
                    }
                }
            }
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            startLocationUpdates(locationRequest)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        100)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
    }

    private fun handleWeatherLayoutClick() {
        if (checkLocationPermission()) {
            Timber.tag("Location.Msg").d("Updating location")
            getLastKnownLocation()
        } else {
            locationPermissionRequestLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun initLaunchers() {
        locationPermissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    // Only approximate location access granted.
                    Timber.tag("Location.Msg").d("Permission granted")
                    getLastKnownLocation()
                }
                else -> {
                    // No location access granted.
                    Timber.tag("Location.Msg").d("Permission denied")
                    Toast.makeText(this, "Location permission needed to fetch weather data!", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun getCityName(lat: Double, lng: Double): String {
        val gcd = Geocoder(this, Locale.ENGLISH)
        val addresses = gcd.getFromLocation(lat, lng, 1)
        return if (addresses.isNotEmpty()) {
            addresses.first().locality
        } else {
            "Unknown"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

val WEATHER_DATA_AVAILABLE = booleanPreferencesKey("weather_data_available")
val WEATHER_LAT = doublePreferencesKey("weather_latitude")
val WEATHER_LNG = doublePreferencesKey("weather_longitude")
val WEATHER_CITY_NAME = stringPreferencesKey("weather_city_name")

