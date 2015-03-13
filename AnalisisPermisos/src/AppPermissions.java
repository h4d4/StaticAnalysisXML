import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author h4d4
 *
 */
public class AppPermissions {
//  /home/h4d4/Development/android-sec-tutorial-src/
//  FriendSeed@FriendTracker@FriendViewer
	static int appProvider = 0; //total apps that implements at less one provider
	static Map<String,NodeList> nameAppPermissions = new HashMap<String,NodeList>();
	static Map<String,String> permissionsLinkApp =  new HashMap<String,String>();
	static NodeList newPermissions;
	static Element root, permission;
	static ArrayList<Element> rootDocList = new ArrayList<Element>();
	static ArrayList<String> info = new ArrayList<String>(), manifestDir = new ArrayList<String>();
	static ArrayList<File> manifestF = new ArrayList<File>();
	static String dirIn, appsName[];
	static File  fXManifest, fXmlFile, inDir, listApps[];
	FileFilter fileFilter;
	/**
	 * 
	 */
	public AppPermissions() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Function ReadPath
	 * Gives project directory to analize, and get path of manifest file to analize
	 * it method works if manifest file name is:AndroidManifest.xml
	 * @throws IOException 
	 */
	private static void ReadPath() throws IOException{
		InputStreamReader lector = new InputStreamReader(System.in); 
		BufferedReader buffer = new BufferedReader( lector );
		System.out.println( "Ingrese el directorio completo donde se alojan las aplicaciones a analizar" );
		dirIn = buffer.readLine();
		inDir = new File( dirIn );
		//inDir = new File( buffer.readLine() );
		listApps = inDir.listFiles();
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		            return file.isDirectory();
		    }
		};
		listApps = inDir.listFiles(fileFilter);
		System.out.println(listApps.length);
	      if (listApps.length == 0) {
	          System.out.println("Either dir does not exist or is not a directory");
	       }
	       else {
	          for (int i=0; i< listApps.length; i++) {
	             File filename = listApps[i];
	             System.out.println(filename.toString());
	          }
	       }
		if( inDir != null ){
			for( int i = 0; i < listApps.length; i++ ){
				manifestDir.add( inDir+"/"+listApps[i].getName()+"/AndroidManifest.xml");
			}
		}else
			System.out.println( "No ingreso el directorio completo donde se alojan las aplicaciones a analizar.");
		
		for( int i = 0; i < manifestDir.size(); i++ ){
			System.out.println( manifestDir.get(i) );
		}
	}
	/*private static void ReadPath() throws IOException{
		String nameApps;
		InputStreamReader lector = new InputStreamReader(System.in); 
		BufferedReader buffer = new BufferedReader( lector );
		System.out.println( "Ingrese el directorio completo donde se alojan las aplicaciones a analizar" );
		dirIn = buffer.readLine();
		
		if( dirIn != null ){
			System.out.println("Ingrese el nombre de cada una de las aplicaciones a analizar, separadas por el simbolo @");
			nameApps = buffer.readLine();
			if( nameApps != null ){
				appsName = nameApps.split("@");
				for( int i = 0; i < appsName.length; i++ )
					manifestDir.add( dirIn+appsName[i]+"/AndroidManifest.xml");
			}else
				System.out.println( "No ingreso el nombre de las aplicaciones a analizar.");
		}else
			System.out.println( "No ingreso el directorio completo donde se alojan las aplicaciones a analizar.");
	}*/
	/**
	 * Function to check if the permission:android;nameClass nameclass alredy exits
	 */
	private static boolean checkNameApp( ArrayList<String> nameapps, String nameapp ){
		boolean res = false;
		System.out.println( "nameapps.size(): "+nameapps.size() );
		for( int i = 0; i < nameapps.size(); i++ ){
			System.out.println( "i_value: "+i );
			if( nameapps.get(i).equalsIgnoreCase(nameapp) ){
				res =  true;
				i = nameapps.size();
			}
				
		}
		return res;
	}
	/**
	 * Function to convert received Manifesf file as a DOM file 
	 */
	private static void extractRoot( String manifest ){
		fXManifest = new File(manifest);
		fXmlFile = fXManifest;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse( fXmlFile );
					root = (Element) doc.getDocumentElement(); //Extract the root element	
			}catch(Exception e) {
					e.printStackTrace();
			}
	}
	/**
	 * Function that get all permissions implemented by the developer
	 */
	private static void getdevelPermss(){
		if( manifestDir.size() > 0){
			for( int i = 0; i <  manifestDir.size(); i++ ){
				extractRoot( manifestDir.get(i) );
				newPermissions = root.getElementsByTagName("permission");
				nameAppPermissions.put(listApps[i].getName(), newPermissions);
			}		
		}
		for ( Entry<String, NodeList> entry : nameAppPermissions.entrySet() ){
			NodeList tmp;
			String namePermission;
			if( entry.getValue().getLength() > 0 ){ //if it had permissions
				tmp = entry.getValue();
				for( int p = 0; p < tmp.getLength(); p++   ){
					permission = (Element) tmp.item(p);
					namePermission = permission.getAttribute("android:name");
					permissionsLinkApp.put(namePermission, entry.getKey());
					
				}
			}
		}
	}
	/**
	 * @param namePerm name of permission that you want to know the name of app that implement it
	 * @return name of the app
	 */
	private static String getNameApp(String namePerm ){
		return permissionsLinkApp.get(namePerm);
	}
	/**
	 * shows all map permissionsLinkApp 
	 * nameofpermission:nameofapp
	 */
	void showAllPermss(){
		for ( Map.Entry< String,String > entry : permissionsLinkApp.entrySet() ){
			System.out.println( entry.getKey() + " --> " + entry.getValue() );
		}
	}

	/**
	 * Function to init methods 
	 * @throws IOException 
	 */
	static void init() throws IOException{
		ReadPath();
		getdevelPermss();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		init();
		AnalisisApp appAnalysis = new AnalisisApp();
		for( int i = 0; i< manifestDir.size(); i++){
			appAnalysis.setFileIn( manifestDir.get(i) );
			appAnalysis.setDirout( dirIn );
			appAnalysis.info.add("TOTAL APPS:"+manifestDir.size());
			appAnalysis.init();
			if( appAnalysis.nListProvider.getLength() > 0 )
				appProvider += 1;
		}
		System.out.println("# Apps that implements at least one provider: " +appProvider);
	}
//  /home/h4d4/Development/android-sec-tutorial-src/
//  FriendSeed@FriendTracker@FriendViewer
//	/home/h4d4/eclipse/workspace/android-sec-tutorial-src/
// 	/home/h4d4/eclipse/workspace/test3/
//com.antivirus@com.google.android.apps.googlevoice@com.google.android.gm@com.jb.gosms@com.pinger.ppa@org.mozilla.firefox
	///home/h4d4/eclipse/workspace/cw-omnibus-f9ca6bb6543e371d614d8eef8764d85ab1c349efTest/
	//ConstantsLoader@ConstantsPlus@ConstantsSecure@Files@GrantUriPermissions@Lorem@Pipe@Provider
	///home/h4d4/eclipse/workspace/android-sec-tutorial-src/
}
