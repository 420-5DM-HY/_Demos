package husaynhakeem.io.androidroom_crud;

import android.app.Activity;
import android.os.Bundle;


import husaynhakeem.io.androidroom_crud.database.AppDataBase;
import husaynhakeem.io.androidroom_crud.utilities.DataGenerator;
import husaynhakeem.io.androidroom_crud.utilities.Logger;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDataBase database = AppDataBase.getAppDatabase(this);

        DataGenerator.with(database).generatePeople();
        DataGenerator.with(database).generateCats();

        Logger.displayAdressesInLog(database.addressDao().loadAll());
        Logger.displayPersonsInLog(database.personDao().loadAll());
        Logger.displayCatsInLog(database.catDao().loadAll());
    }
}
