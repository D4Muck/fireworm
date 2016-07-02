package at.d4muck.firebaseorm.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        UsersRepository usersRepository = new UsersRepository();

        User u = new User();
        u.email = "joe@doe.com";
        u.name = "Joe Doe";
        usersRepository.set(u);

    }
}
