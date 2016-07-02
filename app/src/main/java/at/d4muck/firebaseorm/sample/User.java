package at.d4muck.firebaseorm.sample;

import at.d4muck.firebaseorm.reflection.annotation.Id;

/**
 * Created by cmuck on 02.07.16.
 */
public class User {
    @Id
    public String id;
    public String name;
    public String email;
}
