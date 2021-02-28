import io.javalin.Javalin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.*;

public class HelloWorld {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);


        app.get("/categories/", ctx -> {
            JSONObject file = new JSONObject();
            Connection c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/ecom",
                            "postgres", "qwerty");
            Statement  stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "select DISTINCT on (category) category, image from product;" );
            while ( rs.next() ) {
                String category = rs.getString("category");
                String image = rs.getString("image");
                file.put(category, image);
            }
            stmt.close();
            c.close();
            ctx.json(file);
        });

        app.post("/login/", ctx -> {
            JSONObject file = new JSONObject();
            try{
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/ecom",
                                "postgres", "qwerty");
                PreparedStatement st = c.prepareStatement("SELECT email FROM users where email=? and password=?");
                st.setString(1, (String) jsonObject.get("email"));
                st.setString(2, (String) jsonObject.get("password"));
                ResultSet rs = st.executeQuery();
                if (rs.next()){
                    if ((jsonObject.get("email")).equals("researchvkj@gmail.com")){
                        file.put("admin", 1);
                    }
                    file.put("success", 1);
                }
                else{
                    file.put("success", 0);
                }
                c.close();

            }
            catch (Exception e){
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });

        app.post("/register/", ctx -> {
            JSONObject file = new JSONObject();
            try{
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/ecom",
                                "postgres", "qwerty");
                Statement stmt = c.createStatement();
                stmt.executeUpdate("create table if not exists users(email VARCHAR primary key not null, " +
                                        "name varchar not null, phone VARCHAR not null , password VARCHAR not null)");
                stmt.close();
                PreparedStatement st = c.prepareStatement("INSERT INTO users(email, name, phone, password)" +
                                                                " values(?, ?, ?, ?)");
                st.setString(1, (String) jsonObject.get("email"));
                st.setString(2, (String) jsonObject.get("name"));
                st.setString(3, (String) jsonObject.get("phone"));
                st.setString(4, (String) jsonObject.get("password"));
                st.executeUpdate();
                st.close();
                c.close();
                file.put("success", 1);
            }
            catch (Exception e){
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });


        app.post("/add/", ctx->{
            JSONObject file = new JSONObject();
            try{
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/ecom",
                                "postgres", "qwerty");
                Statement stmt = c.createStatement();
                stmt.executeUpdate("create table if not exists product(id SERIAL primary key not null, " +
                        "name text not null, description VARCHAR , category VARCHAR not null," +
                        "price REAL not null, image TEXT, quantity INT not null default 0, " +
                        "min_quantity INT not null default 0)");
                stmt.close();
                PreparedStatement st = c.prepareStatement("INSERT INTO product(name, description, category, " +
                        "price, image, quantity, min_quantity) values(?, ?, ?, ?, ?, ?, ?)");
                st.setString(1, (String) jsonObject.get("name"));
                st.setString(2, (String) jsonObject.get("description"));
                st.setString(3, (String) jsonObject.get("category"));
                st.setLong(4, (Long) jsonObject.get("price"));
                st.setString(5, (String) jsonObject.get("image"));
                st.setString(6, (String) jsonObject.get("quantity"));
                st.setString(7, (String) jsonObject.get("min_quantity"));
                st.executeUpdate();
                st.close();
                c.close();
                file.put("success", 1);
            }
            catch (Exception e){
                file.put("success", 0);
            }
            ctx.json(file);
        });
    }

}