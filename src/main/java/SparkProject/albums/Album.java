package SparkProject.albums;

import static spark.Spark.get;
import static spark.Spark.port;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import com.google.gson.Gson;

import spark.Request;
import spark.Response;
import spark.Route;

public class Album implements java.io.Serializable {
	@Override
	public String toString() {
		return "Album [title=" + title + ", id=" + id + ", artist=" + artist + ", year=" + year + "]";
	}

	String title;
	int id;
	String artist;
	String year;

	public Album(String title, String artist, String year, int id) {
		this.title = title;
		this.artist = artist;
		this.year = year;
		this.id = id;
	}

	static void writeFile(ArrayList<Album> AlbumCatalog) {
		try {
			FileOutputStream fileOut = new FileOutputStream("album.data");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(AlbumCatalog);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in album.data");
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	public static void main(String[] args) {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		ArrayList<Album> AlbumCatalog = new ArrayList<Album>();
		AlbumId newId = new AlbumId();
		System.out.println("AlbumCatalog "+ AlbumCatalog.toString());
		port(3001);
		
		// JSON return
		get("/data", (req, res) -> {
			Gson mygson = new Gson();
			String json;
			json =mygson.toJson(AlbumCatalog);
			//System.out.println(json);

//			System.out.println("AlbumCatalog "+ AlbumCatalog.toString());
			return json;
		});

		// display the list of albums as html
		get("/", (req, res) -> {

			JtwigTemplate template = JtwigTemplate.classpathTemplate("twig.html");
			JtwigModel model = JtwigModel.newModel().with("albums", AlbumCatalog);

			return template.render(model);
			// String htmlBody = "";
			//
			// for (int i = 0; i < AlbumCatalog.size(); i++) {
			//
			// htmlBody = htmlBody + "<div> Artist: " +
			// AlbumCatalog.get(i).artist + " Title:"
			// + AlbumCatalog.get(i).title + "</div>";
			// }
			//
			// String html = "<!DOCTYPE
			// html><html><head><h1>Albums</h1></head><body><h2>" + htmlBody
			// + "</h2></body></html>";
			// return html;

		});

		// add a new album to the list
		get("/album/create/:title/:artist/:year", (req, res) -> {
			Album newAlbum = new Album(req.params(":title"), req.params(":artist"), req.params(":year"), newId.setId());
			AlbumCatalog.add(newAlbum);
			Album.writeFile(AlbumCatalog);
			return "Added album " + req.params(":title") + " to list! Id =" + newAlbum.id;
		});

		// return album title by id
		get("/album/get/:id", (req, res) -> {
			int id = Integer.parseInt(req.params(":id"));

			for (int i = 0; i < AlbumCatalog.size(); i++) {
				System.out.println(
						"First Album is " + i + AlbumCatalog.get(i).title + " id is " + AlbumCatalog.get(i).id);
				if (AlbumCatalog.get(i).id == id) {
					return "Are you looking for " + AlbumCatalog.get(i).title;
				}
			}
			return "I can't find an album with an id of " + id;
		});
		// delete album by id
		get("/album/del/:id", (req, res) -> {
			int id = Integer.parseInt(req.params(":id"));

			for (int i = 0; i < AlbumCatalog.size(); i++) {
				if (AlbumCatalog.get(i).id == id) {
					AlbumCatalog.remove(i);
					Album.writeFile(AlbumCatalog);
					return "Removed album " + id;
				}
			}
			return "I can't find an album with an id of " + id;
		});

		// normal way: see named class below
		get("/bye/", new MyRoute());

		// anonymous class
		get("/bye/", new Route() {
			@Override
			public Object handle(Request arg0, Response arg1) throws Exception {
				System.out.println("second request made");
				return "bye world";
			}
		});

		// lambda
		get("/bye/", (req, res) -> {
			System.out.println("third request made");
			return "bye world";
		});
	}
}

class AlbumId {
	int id = 0;

	public int setId() {
		this.id++;
		return this.id;

	}
}

// named class
class MyRoute implements Route {

	@Override
	public Object handle(Request req, Response res) throws Exception {

		System.out.println("first request made");
		return "bye world";
	}
}
