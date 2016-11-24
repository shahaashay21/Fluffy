package database.dbconnetor;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
public class RethinkConnector {

        public static final RethinkDB r = RethinkDB.r;
        private Connection conn;
        private String database;



        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public RethinkConnector(String database){
            this.database = database;
            conn = r.connection().hostname("localhost").port(28015).db(database).connect();
        }

        public Connection getConnection(){
            return conn;
        }

}
