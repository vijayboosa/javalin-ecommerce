import io.javalin.Javalin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.*;

public class HelloWorld {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        SendEmail mailClient = new SendEmail();
        String url = "jdbc:postgresql://localhost:5432/ecom";
        String userId = "postgres";
        String userPwd = "qwerty";


        app.get("/getorders/", ctx -> {
            JSONArray file = new JSONArray();
            Connection c = DriverManager.getConnection(url, "postgres", "qwerty");
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from orders;");
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                Long id = rs.getLong("id");
                String order = rs.getString("details");
                obj.put("id", id);
                obj.put("details", order);
                file.add(obj);
            }
            stmt.close();
            c.close();
            ctx.json(file);
        });

        app.post("/order/", ctx -> {
            JSONObject file = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                String details = ctx.body();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());
                Connection c = DriverManager
                        .getConnection(url,
                                userId, userPwd);
                Statement stmt = c.createStatement();
                stmt.executeUpdate("create table if not exists orders(id SERIAL primary key not null, " +
                        "details TEXT not null)");
                stmt.close();
                PreparedStatement st = c.prepareStatement("INSERT INTO orders(details) values(?)");
                st.setString(1, details);
                st.executeUpdate();
                st.close();
                JSONArray products_details = (JSONArray) jsonObject.get("product_details");
                for (int i = 0; i < products_details.size(); i++) {
                    st = c.prepareStatement("UPDATE product SET quantity=? WHERE id=?");
                    JSONObject obj = (JSONObject) products_details.get(i);
                    st.setLong(1, (Long) obj.get("new_quantity"));
                    st.setInt(2, Integer.parseInt(String.format("%d", obj.get("id"))));
                    st.executeUpdate();
                    st.close();
                    Long mailStatus = (Long) obj.get("mail_status");
                    if (mailStatus == 1) {
                        mailClient.Send(String.format("Quantity for item %s product id %s is really low",
                                obj.get("id"), obj.get("name")), "researchvkj@gmail.com");
                    }

                }
                String toEmail = (String) jsonObject.get("to_email");
                String htmlData = (String) jsonObject.get("html_data");
                c.close();
                mailClient.Send(htmlData, toEmail);
                file.put("success", 1);
            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });

        app.get("/categories/", ctx -> {
            JSONObject file = new JSONObject();
            Connection c = DriverManager.getConnection(url, "postgres", "qwerty");
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select DISTINCT on (category) category, image from product;");
            while (rs.next()) {
                String category = rs.getString("category");
                String image = rs.getString("image");
                file.put(category, image);
            }
            stmt.close();
            c.close();
            ctx.json(file);
        });

        app.get("/products/", ctx -> {
            JSONArray file = new JSONArray();
            Connection c = DriverManager.getConnection(url, "postgres", "qwerty");
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from product;");
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                Long id = rs.getLong("id");
                String category = rs.getString("category");
                String image = rs.getString("image");
                String description = rs.getString("description");
                String name = rs.getString("name");
                Long price = rs.getLong("price");
                Long quantity = rs.getLong("quantity");
                Long min_quantity = rs.getLong("min_quantity");
                obj.put("id", id);
                obj.put("category", category);
                obj.put("image", image);
                obj.put("description", description);
                obj.put("name", name);
                obj.put("price", price);
                obj.put("quantity", quantity);
                obj.put("min_quantity", min_quantity);
                file.add(obj);
            }
            stmt.close();
            c.close();
            ctx.json(file);
        });

        app.put("/update/", ctx -> {
            JSONObject file = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection(url,
                                "postgres", "qwerty");
                PreparedStatement st = c.prepareStatement("UPDATE product SET name=?, description=?, category=?," +
                        "price=?, quantity=?, min_quantity=? WHERE id=?");
                st.setString(1, (String) jsonObject.get("name"));
                st.setString(2, (String) jsonObject.get("description"));
                st.setString(3, (String) jsonObject.get("category"));
                st.setLong(4, (Long) jsonObject.get("price"));
                st.setLong(5, (Long) jsonObject.get("quantity"));
                st.setLong(6, (Long) jsonObject.get("min_quantity"));
                st.setLong(7, (Long) jsonObject.get("id"));
                st.executeUpdate();
                st.close();
                c.close();
                file.put("success", 1);
            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });

        app.delete("/delete/:id", ctx -> {
            JSONObject file = new JSONObject();
            int productId = Integer.parseInt(ctx.pathParam("id"));
            try {
                Connection c = DriverManager
                        .getConnection(url,
                                "postgres", "qwerty");
                PreparedStatement st = c.prepareStatement("DELETE from product where id=?");
                st.setInt(1, productId);
                st.executeUpdate();
                st.close();
                c.close();
                file.put("success", 1);
            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
        });

        app.post("/login/", ctx -> {
            JSONObject file = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection(url,
                                "postgres", "qwerty");
                PreparedStatement st = c.prepareStatement("SELECT email FROM users where email=? and password=?");
                st.setString(1, (String) jsonObject.get("email"));
                st.setString(2, (String) jsonObject.get("password"));
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    if ((jsonObject.get("email")).equals("researchvkj@gmail.com")) {
                        file.put("admin", 1);
                    }
                    file.put("success", 1);
                } else {
                    file.put("success", 0);
                }
                st.close();
                c.close();

            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });

        app.post("/register/", ctx -> {
            JSONObject file = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection(url,
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
            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });


        app.post("/add/", ctx -> {
            JSONObject file = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(ctx.body());

                Connection c = DriverManager
                        .getConnection(url,
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
                st.setLong(6, (Long) jsonObject.get("quantity"));
                st.setLong(7, (Long) jsonObject.get("min_quantity"));
                st.executeUpdate();
                st.close();
                c.close();
                file.put("success", 1);
            } catch (Exception e) {
                System.out.println(e);
                file.put("success", 0);
            }
            ctx.json(file);
        });
    }
}

interface Order {

}

interface Product {
    String name = null;


}