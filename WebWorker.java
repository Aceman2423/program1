/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

//my entered methods
import java.util.*;
import java.io.File;
import java.lang.Object;

public class WebWorker implements Runnable
{

private Socket socket;

// my added variables
private int ErrorFound =3;
private String str = "";
Calendar calendar = Calendar.getInstance();


/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      writeHTTPHeader(os,"text/html");

      if ( ErrorFound == 0 ) {
    	  Error404(os);
      }
      else if ( ErrorFound == 1 ) {
	  WriteFileHtml( os, str );
      }
      else {
          writeContent(os);
      }

      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/

//changed void to String

private void readHTTPRequest(InputStream is)
{
   String line, file;// str = null;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;

	 if ( line.substring(0,3).compareTo("GET") == 0 ){ 

	     if ( line.substring(4, line.length() - 9 ).length() != 1 ) {
	        file = line.substring(5, line.length() - 9 );
	        File htmlfile = new File( file );


	        if ( htmlfile.isFile() ){
	           Scanner reader = new Scanner( htmlfile );
	  	   str = "";
		   while ( reader.hasNext() ){
		      str = str + reader.nextLine();
		   } // end while
		   ErrorFound = 1;

		   // string tag replacing
		   String date = formatDate( calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR) );
		   str = str.replaceAll( "<cs371date>", date );
		   str = str.replaceAll( "<cs371server>", "Brandons Server" );

	        }
	        else {
		   ErrorFound = 0;
	        } // end else
	     
	     } // end line substring == 0

         } // end if line substring get

      } catch (Exception e) {
         System.err.println("Request error: "+e);
    	 ErrorFound = 0;
         break;
      }
   }
   return;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write("HTTP/1.1 200 OK\n".getBytes());
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/*

*/
private String formatDate( int date, int month, int year ){
    String[] months = { "January", "Febuary", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

    String sb = months[month] + " " + date + ", " + year ;
    return sb;

}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void Error404(OutputStream os) throws Exception{
	 os.write("<html><head></head><body>\n".getBytes());
	 os.write("<h3>404 Not Found<h3>\n".getBytes());
	 os.write("</body></html>\n".getBytes());
	
}
private void writeContent(OutputStream os) throws Exception
{
   os.write("<html><head></head><body>\n".getBytes());
   os.write("<h3>My web server works!</h3>\n".getBytes());
   os.write("</body></html>\n".getBytes());
}
private void WriteFileHtml( OutputStream os, String output ) throws Exception{
   
   os.write("<html><head></head><body>\n".getBytes());
   os.write(output.getBytes());
   os.write("</body></html>\n".getBytes());
}
} // end class
