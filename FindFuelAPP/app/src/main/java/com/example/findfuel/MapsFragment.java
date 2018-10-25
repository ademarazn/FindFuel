package com.example.findfuel;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findfuel.directions.FetchUrl;
import com.example.findfuel.nearbyplaces.GetNearbyPlacesData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mirrorlink.android.commonapi.IConnectionListener;
import com.mirrorlink.android.commonapi.IConnectionManager;
import com.mirrorlink.lib.MirrorLinkApplicationContext;
import com.mirrorlink.lib.ServiceReadyCallback;

import java.util.ArrayList;

/**
 * @author Ademar
 * @since Classe criada em 11/10/2017
 */
public class MapsFragment extends MapFragment implements OnMapReadyCallback,
        LocationListener {

    /* Variáveis utilizadas */
    public final String TAG = "MapsFragment";
    MirrorLinkApplicationContext mMirrorLinkContext = null;
    private GoogleMap map;
    private boolean first = true;
    LocationManager locationManager;
    int PROXIMITY_RADIUS = 5000;
    IConnectionManager mConnectionManager = null;
    IConnectionListener mConnectionManagerListener = new IConnectionListener.Stub() {
        @Override
        public void onMirrorLinkSessionChanged(boolean connected) throws RemoteException {
            // Mostra o status da conexão com o MirrorLink
            showMirrorLinkConnectionStatus(connected);
        }

        @Override
        public void onAudioConnectionsChanged(Bundle bundle) throws RemoteException {
            // do-nothing
        }

        @Override
        public void onRemoteDisplayConnectionChanged(int i) throws RemoteException {
            // do-nothing
        }
    };

    /* Construtor vazio Default */
    public MapsFragment() {
    }

    /*
    Método que faz parte do ciclo de vida de um Fragment e, que será chamado
    quando for criar a View no fragmento
    */
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        getMapAsync(this);
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    /*
    Método onCreate faz parte do ciclo de vida da Activity e do Fragment
    será chamado quando for criar a Activity, lembrando que um Fragment não funciona por si só,
    precisando assim sempre de uma Activity para o inflar dentro dela e, se ela for destruída,
    o fragmento também será destruído
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            ApplicationContext.getContext(new ServiceReadyCallback() {
                @Override
                public void connected(MirrorLinkApplicationContext mirrorLinkApplicationContext) {
                    mConnectionManager = mirrorLinkApplicationContext.registerConnectionManager(this, mConnectionManagerListener);

                    try {
                        showMirrorLinkConnectionStatus(mConnectionManager.isMirrorLinkSessionEstablished());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Service connected and ready to use");
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "MapsFragment onCreate: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /* Método para mostrar uma mensagem Toast avisando de possui ou não conexão com o MirrorLink */
    private void showMirrorLinkConnectionStatus(boolean connected) {
        if (connected)
            Toast.makeText(getActivity(), "MirrorLink is connected", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "MirrorLink is not connected", Toast.LENGTH_SHORT).show();
    }

    /*
    O método onDestroy também faz parte do ciclo de vida de um Fragment e,
    será chamado ao destruir o fragmento
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMirrorLinkContext != null) {
            mMirrorLinkContext.unregisterConnectionManager(this, mConnectionManagerListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationManager == null) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        }
        requestLocationUpdates();
    }

    /* Método que será chamado quando o mapa estiver pronto para ser usado */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            this.map = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.clear();
//        LatLng latLng = new LatLng(-22.97793254, -49.86847222);
//        updateLocation(latLng);

        /* Listener que ouvirá os cliques no botão My Location do mapa */
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    //LatLng latLng1 = map.getProjection().getVisibleRegion().latLngBounds.getCenter();
//                    Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    requestLocationUpdates();
//                    if (map != null && l != null) {
//                        updateLocation(new LatLng(l.getLatitude(), l.getLongitude()));
//                    }
                    return false;
                }
            });

        /* Listener que ouvirá os cliques nos marcadores do mapa */
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });

        /* Listener que ouvirá os cliques nas janelas de informação dos marcadores do mapa */
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    try {
                        // mostrar rota
                        Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        String origem = l.getLatitude() + "," + l.getLongitude();
                        String destino = marker.getPosition().latitude + "," + marker.getPosition().longitude;
                        if (marker.getTitle().equals("Seu local")) {
                            return;
                        }
                        updateLocation(new LatLng(l.getLatitude(), l.getLongitude()));
                        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origem +
                                "&destination=" + destino + "&sensor=true&mode=driving&key=" +
                                getResources().getString(R.string.google_maps_key);
                        //Toast.makeText(getContext(), "MapsFragment onInfoWindowClick: " + url, Toast.LENGTH_LONG).show();
                        Log.d("url direction", url);
                        FetchUrl fetchUrl = new FetchUrl();
                        //Toast.makeText(getContext(), "MapsFragment onInfoWindowClick: 2", Toast.LENGTH_SHORT).show();
                        Object dataTransfer[] = new Object[3];
                        dataTransfer[0] = map;
                        dataTransfer[1] = url;
                        ArrayList<String> data = fetchUrl.execute(dataTransfer).get();
                        if (data.size() == 3) {
                            TextView textView = (TextView) getActivity().findViewById(R.id.dados);
                            textView.setText(data.get(2) + "\n" + data.get(1));
                            textView.setVisibility(TextView.VISIBLE);
                        }
                        //Toast.makeText(getContext(), "MapsFragment onInfoWindowClick: 3", Toast.LENGTH_SHORT).show();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 13));
                        //Toast.makeText(getContext(), "MapsFragment onInfoWindowClick: movido", Toast.LENGTH_SHORT).show();
                        removeUpdates();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "MapsFragment onInfoWindowClick: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "MapsFragment onMapReady: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void requestLocationUpdates() {
        removeUpdates();
        // o método para raceber atualizações do GPS irá receber será atualizado a cada
        // 0 milissegundos e irá atualizar a localização quando
        // a distância percorrida for de 50 metros.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, this);
    }

    private void removeUpdates() {
        locationManager.removeUpdates(this);
    }

    /*
    Método para atualizar a localização no mapa e que também utiliza um objeto da classe
    GetNearbyPlacesData para atualizar os postos de combustíveis próximos
    */
    private void updateLocation(LatLng latLng) {
        try {
            map.clear();
            TextView txt = (TextView) getActivity().findViewById(R.id.dados);
            txt.setText("");
            txt.setVisibility(TextView.INVISIBLE);
            if (hasInternetConnection()) {
                String gasStation = "gas_station";
                String url = getUrl(latLng.latitude, latLng.longitude, gasStation);
                Log.d("url", url);
                Object dataTransfer[] = new Object[2];
                dataTransfer[0] = map;
                dataTransfer[1] = url;

                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(getActivity(), "Mostrando postos de combustíveis próximos", Toast.LENGTH_SHORT).show();
                map.addMarker(new MarkerOptions()
                        .title("Seu local")
                        .snippet("Você está aqui")
                        .position(latLng)).showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                first = false;
            } else {
                Toast.makeText(getActivity(), "Sem conexão com a Intenet", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "MapsFragment updateLocation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*
    Método para obter uma String com a URL da API do Google Places - NearbySearch
    que por sua vez returnará um arquivo JSON
    */
    private String getUrl(double latitude, double longitude, String nearbyPlaces) {
        // radius define o raio máximo da busca
        // opennow=true mostra somente os postos de combustíveis abertos agora
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=" + PROXIMITY_RADIUS +
                "&type=" + nearbyPlaces +
                "&opennow=true" +
                "&sensor=true" +
                "&key=" + getResources().getString(R.string.google_maps_key);
    }

    /* Método para verificar a existência de conexão com a internet */
    public boolean hasInternetConnection() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo() != null
                    && connectivityManager.getActiveNetworkInfo().isAvailable()
                    && connectivityManager.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "MapsFragment hasInternetConnection: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /* Método que será chamado pelo LocationListener quando mudar a localização */
    @Override
    public void onLocationChanged(Location location) {
        if (map != null && location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            updateLocation(new LatLng(latitude, longitude));
        }
    }

    /* Método que será chamado pelo LocationListener quando mudar o status */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /* Método que será chamado pelo LocationListener quando um provedor estiver ativado */
    @Override
    public void onProviderEnabled(String provider) {
        Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (map != null && l != null && provider.equals(LocationManager.GPS_PROVIDER) && first) {
            updateLocation(new LatLng(l.getLatitude(), l.getLongitude()));
        }
    }

    /* Método que será chamado pelo LocationListener quando um provedor for desativado */
    @Override
    public void onProviderDisabled(String provider) {
    }
}
