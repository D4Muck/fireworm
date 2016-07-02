package at.d4muck.firebaseorm.sample;

import at.d4muck.firebaseorm.Repository;

/**
 * Created by cmuck on 02.07.16.
 */
public class UsersRepository extends Repository<User> {
    public UsersRepository() {
        super(User.class);
    }
}
