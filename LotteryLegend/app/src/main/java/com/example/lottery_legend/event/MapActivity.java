package com.example.lottery_legend.event;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_MARKER_NAME = "marker_name";

    // Multiple markers
    public static final String EXTRA_LATITUDES = "latitudes";
    public static final String EXTRA_LONGITUDES = "longitudes";
    public static final String EXTRA_NAMES = "names";

    // Pick mode
    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String RESULT_LATITUDE = "result_latitude";
    public static final String RESULT_LONGITUDE = "result_longitude";
    public static final String RESULT_ADDRESS = "result_address";

    // Default location: Edmonton
    private static final LatLng DEFAULT_LOCATION = new LatLng(53.5461, -113.4938);
    private static final float DEFAULT_ZOOM = 15f;
    private static final float MULTI_MARKER_PADDING = 150f;

    private GoogleMap mMap;
    private String title;
    private boolean isPickMode;
    private Marker pickMarker;
    private MaterialButton btnConfirmPick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        title = getIntent().getStringExtra(EXTRA_TITLE);
        isPickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);

        if (title == null || title.trim().isEmpty()) {
            title = isPickMode ? "Pick Location" : "Location Map";
        }

        setupToolbar();
        setupConfirmButton();
        setupMapFragment();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarMap);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupConfirmButton() {
        btnConfirmPick = findViewById(R.id.btnConfirmPick);
        btnConfirmPick.setVisibility(isPickMode ? View.VISIBLE : View.GONE);
        btnConfirmPick.setOnClickListener(this::onConfirmPick);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            Toast.makeText(this, "Map fragment not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls and gestures
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (isPickMode) {
            setupPickMode();
        } else {
            setupViewMode();
        }
    }

    private void setupPickMode() {
        double initialLat = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
        double initialLng = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);

        LatLng initialPos;
        if (initialLat != 0 || initialLng != 0) {
            initialPos = new LatLng(initialLat, initialLng);
        } else {
            initialPos = DEFAULT_LOCATION;
        }

        pickMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(initialPos)
                        .title("Selected Location")
                        .draggable(true)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, DEFAULT_ZOOM));

        mMap.setOnMapClickListener(latLng -> {
            if (pickMarker != null) {
                pickMarker.setPosition(latLng);
            }
        });
    }

    private void setupViewMode() {
        ArrayList<Double> lats = getDoubleArrayList(EXTRA_LATITUDES);
        ArrayList<Double> lngs = getDoubleArrayList(EXTRA_LONGITUDES);
        ArrayList<String> names = getIntent().getStringArrayListExtra(EXTRA_NAMES);

        if (lats != null && lngs != null && !lats.isEmpty() && lats.size() == lngs.size()) {
            showMultipleMarkers(lats, lngs, names);
        } else {
            showSingleMarker();
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private ArrayList<Double> getDoubleArrayList(String key) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return (ArrayList<Double>) getIntent().getSerializableExtra(key, ArrayList.class);
            } else {
                return (ArrayList<Double>) getIntent().getSerializableExtra(key);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void showSingleMarker() {
        double lat = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
        double lng = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);
        String name = getIntent().getStringExtra(EXTRA_MARKER_NAME);

        if (name == null || name.trim().isEmpty()) {
            name = "Event Location";
        }

        if (lat == 0 && lng == 0) {
            Toast.makeText(this, "No location coordinates provided", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            return;
        }

        LatLng location = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(location).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }

    private void showMultipleMarkers(ArrayList<Double> lats, ArrayList<Double> lngs, ArrayList<String> names) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasPoints = false;

        for (int i = 0; i < lats.size(); i++) {
            Double lat = lats.get(i);
            Double lng = lngs.get(i);

            if (lat == null || lng == null) continue;
            if (lat == 0 && lng == 0) continue;

            LatLng point = new LatLng(lat, lng);
            String name = (names != null && names.size() > i && names.get(i) != null && !names.get(i).trim().isEmpty())
                    ? names.get(i)
                    : "Entrant";

            mMap.addMarker(new MarkerOptions().position(point).title(name));
            builder.include(point);
            hasPoints = true;
        }

        if (!hasPoints) {
            Toast.makeText(this, "No valid locations provided", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            return;
        }

        mMap.setOnMapLoadedCallback(() -> {
            try {
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) MULTI_MARKER_PADDING));
            } catch (IllegalStateException ignored) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            }
        });
    }

    public void onConfirmPick(View view) {
        if (!isPickMode) return;

        if (pickMarker == null) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng pos = pickMarker.getPosition();
        String address = getAddressFromLatLng(pos);

        Intent data = new Intent();
        data.putExtra(RESULT_LATITUDE, pos.latitude);
        data.putExtra(RESULT_LONGITUDE, pos.longitude);
        data.putExtra(RESULT_ADDRESS, address);

        setResult(RESULT_OK, data);
        finish();
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                    sb.append(addr.getAddressLine(i));
                    if (i < addr.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                String result = sb.toString().trim();
                if (!result.isEmpty()) {
                    return result;
                }
            }
        } catch (IOException ignored) {
        } catch (Exception ignored) {
        }

        return String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude);
    }
}