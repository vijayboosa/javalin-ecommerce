import io.javalin.Javalin;
import org.json.simple.JSONObject;

public class HelloWorld {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> {
           ctx.result("We are running");
        });
        app.get("/register/", ctx -> {
            JSONObject file = new JSONObject();
            file.put("Full Name", "Ritu Sharma");
            file.put("Roll No.", 1704310046);
            file.put("Tution Fees", 65400.0);
            ctx.json(file);
        });
    }

}