import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
suspend fun getCurrentLocation(context: Context): LatLng? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    return try {
        val location = fusedLocationClient.lastLocation.await()
        location?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}