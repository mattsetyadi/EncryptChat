package se.joscarsson.privify.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;

import se.joscarsson.privify.R;

public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    protected int getMenuItemId() {
        return R.id.settings_menu_item;
    }
}
