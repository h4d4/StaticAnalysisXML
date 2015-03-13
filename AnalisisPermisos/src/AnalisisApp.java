import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.io.*;

// /home/h4d4/Development/android-sec-tutorial-src/FriendViewer/AndroidManifest.xml
// /home/h4d4/Development/android-sec-tutorial-src/FriendTracker/AndroidManifest.xml
///home/h4d4/Development/android-sec-tutorial-src/FriendSeed/AndroidManifest.xml
/**
 * @author h4d4
 *
 */
/**
 * @author h4d4
 *
 */
public class AnalisisApp {
	private static AppPermissions app_permissions = new AppPermissions();
	static boolean exported = true; //por default it is true, if it change to false, meaning that exported atribute is equal to false
	static int numAct = 0, numSer = 0, numRec = 0, actFilt = 0, serFilt =0, recFilt =0;
	static ArrayList<Integer>attribSize = new ArrayList<Integer>();
	static Map<String,Integer>dates = new HashMap<String,Integer>();
	static Map<String,String>secAttribTrue = new HashMap<String,String>(), secAttribFalse = new HashMap<String,String>(), 
			permmisionAttrib = new HashMap<String,String>(), providerMsn = new HashMap<String,String>();//Maps to save attributes that have security meaning, defined for a component
	static File  fXManifest, fXmlFile, xmlString, xmlStringFile;
	static Element root, component, permission, rootString,appName, stringName, provider, sdk;
	static NodeList nListActivity,elementsFilter,newPermissions,usePermissions, nListComp, nListTmp, nListStr, nListProvider, sdkList;//nListFilters save filters founded
	static Node filter, activityNode;
	static String fileIn, fileOut, dirOut; 
	static ArrayList<NodeList> nListFilters = new ArrayList<NodeList>();
	static ArrayList< ArrayList<NamedNodeMap> > attributes = new ArrayList< ArrayList<NamedNodeMap> >();
	static ArrayList<String> info = new ArrayList<String>(), msgActivity = new ArrayList<String>(), msgPermission = new ArrayList<String>();
	static ArrayList<String> data = new ArrayList<String>(), debug = new ArrayList<String>();
	public static void setFileIn( String inFile ){
		fileIn = inFile;
	}
	public static void setDirout( String outDir ){
		dirOut = outDir;
	}
	
	/**
	 * Function to full  msgActivity ArrayList than have all messages to print in the file
	 * Saved values as below order:
	     *0: don't defined Intent Filters attributes and don't defined exported attributed
		 * 1: don't defined Intent Filters attributes but defined exported attributed as true
		 * 2: defined exported attributed as true
		 * 3: good filters
		 * 4: Default filters for main activity
		 * 5: defined exported attributed as false
		 * 6: android:permission attribute is not defined
		 * 7: android:permission attribute defined
	 */
	private static void fullingMsnActivity(){
		msgActivity.add("WARNING: This component don't defined Intent filters and don't defined exported attributed. "
				+ "In theory, this component only can be invoked for components from the same app, since others would not know the class name"
				+ " WARNING: An malware app could be found the name class and invoked this component. " );
		msgActivity.add("WARNING: This component don't defined Intent filters and  defined exported attributed as true. "
				+ "This meaning that any external app could be invoked this component");
		msgActivity.add("The android:exported attribute is true. ");
		msgActivity.add("This component can be invoked by external apps, only if arrived intents match with the follow FILTERS: ");
		msgActivity.add("This it the main Activity. It defined default filters for its functionality.");
		msgActivity.add("The android:exported attribute is false.");
		msgActivity.add("The android:permission attribute is not defined.");
		msgActivity.add("The android:permission attribute defined are:");
	}
	/**
	 * Function to full  msgPermission Map
	 * msgPermission Map save all security aspects and its security meaning, of permissions defined by the developer
	 * true mining that the attribute is defined
	 */
	private static void fullingPermissionAtt(){
		permmisionAttrib.put( "permissions_true","Developer has implemented the follow permissions:" );
		permmisionAttrib.put( "permissions_false", "WARNING: Developer didn't implement permissions for this app" );
		permmisionAttrib.put("permissionGroup_true","This permission has defined permissionGroup attributed. It meaning that this permission will be Assign to a group" );
		permmisionAttrib.put("permissionGroup_false","This permission hasn't defined permissionGroup attributed. It meaning that this permission does not belong to a group" );
		permmisionAttrib.put("protectionLevel_false","This permission hasn't defined protectionLevel attributed then, by default, the risk level of this permission is: lower. "
				+ "It meaning that permission features are: "+"\n"
		+"Potencial risk permission: lower. Grants by: System. Meaning: gives requesting applications access to isolated application-level features, with minimal risk to other applications, the system, or the user.");
		permmisionAttrib.put( "normal_true","Normal level potencial risk are lower. Grants by: System. "
				+ "Meaning: gives requesting applications access to isolated application-level features, with minimal risk to other applications, the system, or the user.  " );
		permmisionAttrib.put( "dangerous_true", "Dangerous level potencial risk are higher. Grants by: user. "
				+ "Meaning: gives requesting application access to private user data or control over the device that can negatively impact the user.");
		permmisionAttrib.put( "signature_true", "Grants by: System. Meaning: grants only if the requesting application is signed with the same certificate as the application that declared the permission.");
		permmisionAttrib.put( "signatureOrSystem_true", "Grants by: System. Meaning: system grants only to applications that are in the Android system image or that are signed with the same certificate as the application that declared the permission");
	}
	/**
	 * Function to full  secAttribTrue Map
	 * secAttribTrue Map save all security attributes and its security meaning when are set as TRUE in the component
	 *  NEED TO ASK IF THE ATRIBUTES ARE SET AS true!!!!!!
	 */
	private static void fullingAttbMapT(){
		secAttribTrue.put("android:allowEmbedded","Activity can be actived as a child of other activity (Execution privilege.)");
		secAttribTrue.put("android:exported","The activity can be launched(actived) by components of other applications.");
	}
	/**
	 * Function to full  secAttribFalse Map
	 * secAttribTrue Map save all security attributes and its security meaning when are set as FALSE in the component
	 */
	private static void fullingAttbMapF(){
		secAttribFalse.put("android:allowEmbedded","Activity can't be actived as a child of other activity (Execution privilege denied).");
		secAttribFalse.put("android:exported","The Activity can't be invoked by components of other applications.");
	}
	/**
	 * Function to full all messages about provider component analysis
	 */
	private static void fullingProviderMsn(){
		providerMsn.put("exp_notdefined", "The default value for exported attribute dependes of the sdk version that use it app:"
				+ " if application set either minSdkVersion or targetSdkVersion to 16 or lower, the default value is true. "
				+ " If application set either minSdkVersion or targetSdkVersion to 17 or lower, the higher the default value is false.");
		providerMsn.put("exp_true_notpermission", "WARNING: you didn't define permissions to this content provider, this mean that any extern application can access its conten only especied the URI ");
		providerMsn.put("exp_true_okpermission", "WARNING: Any application can uses the provider if especifies the permission bellow; "
				+ "but you're grant read and write permissions. You should be divided its two.  ");
		providerMsn.put("exp_true_okreadpermission", "Data on this Content Provider can be read by applications that defined this permission:");
		providerMsn.put("exp_true_okwritepermissions", "Data on this Content Provider can be  write by applications that defined this permission:");
		providerMsn.put("exp_und","The default value of atributed exported is nof identified. Please, be sure that you defined its value.");
		providerMsn.put("exp_false","This provider can't no be exported.");
	}
	/**
	 * Function to convert received Manifesf file as a DOM file 
	 */
	private static void extractRoot( String manifest){
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
	private static void extractRootString( String stringFile){
		xmlString = new File(stringFile);
		xmlStringFile = xmlString;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse( xmlStringFile );
				rootString = (Element) doc.getDocumentElement(); //Extract the root element	
			}catch(Exception e) {
					e.printStackTrace();
			}
	}
	/**
	 * Function that search given symbol in given string vector
	 * @return last position of the symbol
	 */
	static int findSymbol(String symbol, String cadena ){
		char vector[] = cadena.toCharArray();
		int pos = 0;
		for( int i = 0; i< vector.length; i++ ){
			if( Character.toString( vector[i]).equalsIgnoreCase("/") )
				pos = i;	
		}
		return pos;
	}
	private static String getAppName(){//**************************************************
		String nameRet = null, name, label, labelItems[] = null;
		nListTmp = (NodeList) root.getElementsByTagName( "application" );
		System.out.println("NOMBRE DE LA APLICACION: "+nListTmp.item(0));
		appName = (Element)nListTmp.item(0);
		name = appName.getAttribute("android:name");
		System.out.println("nameValue"+name+" " );
		if( name == "" ){
			label = appName.getAttribute("android:label");
			labelItems = label.split("/");
			if( labelItems[0].equalsIgnoreCase( "@string" ) ){
				extractRootString( fileIn.substring(0, findSymbol("",fileIn)+1 )+"res/values/strings.xml"  );
				nListStr =  (NodeList)rootString.getElementsByTagName( "string" );
				for( int i =0 ; i< nListStr.getLength(); i++ ){
					stringName = (Element)nListStr.item(i);
					if( stringName.getAttribute("name").equalsIgnoreCase( labelItems[1] ) )
						nameRet = stringName.getTextContent();
				}
			}else
				nameRet = "application name not found";
		}else{
			nameRet = name;
		}
		return nameRet;
	}
	/**
	 * Function to extract all components of type of nameComponent pass as argument
	 * check if the allowEmbedded attribute and, exported attributed are defined in every component
	 * Checks if intent filters are defined in the component, and then define exported attributed value, two principals branch are:
	 	*If exits intent filters
	 		*And exported attributed is not defined:
	 		In this case, check if is the main activity and print msgActivity(4)
	 		else print: msgActivity(3) andits Intent Filters;
	 		*And exported attributed is defined as true
	 		In this case, print: msgActivity(2), msgActivity(3) and its Intents Filters;	
	 	*If not exits intent filters
	 		*And exported attributed is not defined:
	 		In this case, print: msgActivity(3);
	 		*And exported attributed is defined as true:
	 		In this case, print: msgActivity(0);	
	 * 	
	 */
	private static void components( String componetName ){
		int pilot = 0;
		String allowEmbedded, exported, grantPermiss;
		ArrayList <String> filters = new ArrayList <String>();
		boolean exp = false, actComp = false, servComp = false, recComp = false; //boolean vars to know where type of component is analyzing
		int filter_exit = 0, val_exp = 0; // 1: exp=true; 2: exp=false; 3: exp="";
		nListComp = root.getElementsByTagName( componetName );//get every nodes of input component
		if( componetName.equalsIgnoreCase("activity") ){
			actComp =  true;
			numAct = nListComp.getLength(); System.out.println("numAct: "+numAct );
			
		}
		if( componetName.equalsIgnoreCase("service") ){
			servComp = true;
			numSer = nListComp.getLength(); System.out.println("numSer: "+numSer );
		}
		if( componetName.equalsIgnoreCase("receiver") ){
			recComp = true;
			numRec = nListComp.getLength(); System.out.println("numRec: "+numRec );
		}
		for( int i=0; i< nListComp .getLength(); i++  ){
			component = (Element) nListComp.item(i); //tomo cada nodo de la lista
			info.add( "----> Component type: " + componetName);
			info.add("Component Name: "+component.getAttribute("android:name"));
			if( actComp ){			//only if the component is an activity, will be check android:allowEmbedded attribute 
				allowEmbedded = component.getAttribute( "android:allowEmbedded" );
				if( ! allowEmbedded.equalsIgnoreCase("") ){// allowEmbedded is defined
					if( allowEmbedded.equalsIgnoreCase("true") )
						info.add( "The android:allowEmbedded attribute is true. " + secAttribTrue.get("android:allowEmbedded") );
					else
						info.add( "The android:allowEmbedded attribute is false. " + secAttribFalse.get("android:allowEmbedded") );
				}else{// allowEmbedded is NOT defined
					info.add( "The android:allowEmbedded attribute is NOT defined. " );
				}
			}
			exported = component.getAttribute( "android:exported" );
			grantPermiss = component.getAttribute("android:permission");
			
			if( ! exported.equalsIgnoreCase("") ){ //if exported attribute id defined
				if( exported.equalsIgnoreCase("true") )
					val_exp = 1;
				if( exported.equalsIgnoreCase("false") ){
					info.add( msgActivity.get(5) + secAttribFalse.get("android:exported") );//not matter if defined filters
					val_exp = 2;
				} 
			}else//exported attribute is NOT defined  //VERIFICAR EL CASO CUANDO SE TRATA DE ACTIVITY MAIN, PARA DESCARTAR 
				val_exp = 3;
			
			if( ! grantPermiss.equalsIgnoreCase("") ){//if android:permission attribute is set
				info.add( msgActivity.get(7) + grantPermiss );
			}else
				info.add(msgActivity.get(6)); 
			filter_exit = component.getElementsByTagName("intent-filter").getLength();
			//filter_exit = component.getElementsByTagName("intent-filter").getLength();
			System.out.println("FILTTERS FOR component i "+i+": "+filter_exit+"  "+component.getAttribute("android:name"));
			
			if( filter_exit > 0 ){//if this component have filters
				nListFilters.add(  component.getElementsByTagName("intent-filter") );
				setFilters();
				if( actComp ){
					pilot = i; actFilt = actFilt + 1 ;}
				if( servComp ){
					pilot = i + numAct; serFilt = serFilt + 1 ;}
				if( recComp ){
					pilot = i + numAct + numSer; recFilt = recFilt + 1; }
				System.out.println("valueOfPilot: " +pilot);
				filters  = getFilters( pilot, exp, actComp);
				if( val_exp == 3 ){//if exported attribute is not defined 
					if(  filters.size() == 1 && actComp ){	//check if is a main activity
						if( filters.get(0).equalsIgnoreCase("main") ){
							info.add( msgActivity.get(4));
						}
						else{
								info.add( msgActivity.get(3)+"0000");
								info.add( filters.get(0) );
						}
					}else{
						
							String debug =filters.size()+"  "+actComp ;
							info.add( msgActivity.get(3)+"1111"+ debug);
							for( int k=0; k< filters.size(); k++ ) //set the filters defined by activity
								info.add( filters.get(k) );
						
						
					}
				}
				if( val_exp == 1 ){//if attributed exported is defined as true
					info.add( msgActivity.get(2) + secAttribTrue.get("android:exported") );
					info.add( msgActivity.get(3) );
					for( int k=0; k< filters.size(); k++ ) //set the filters defined by activity
						info.add( filters.get(k) );
				}
			}else{//if activity don't have filters
				ArrayList<NamedNodeMap>attbTest=new ArrayList<NamedNodeMap>();
				attbTest.add( component.getAttributes() );
				attributes.add( attbTest );
				   if( val_exp == 3 )//if exported attribute is don't defined
					   info.add( msgActivity.get(0) );
				   if( val_exp == 1 )//if attributed exported is defined as true
					  info.add( msgActivity.get(0) );
			  }
		}
		System.out.println("nListFilters.size"+nListFilters.size());
		if( actComp ){
			info.add( "TOTAL FILTERS BY " + componetName+ " = " + actFilt );
			data.add("IFA:"+ actFilt); 
			info.add( "nListFilters.size"+nListFilters.size() );}
		if( servComp ){
			info.add( "TOTAL FILTERS BY " + componetName+ " = " + serFilt );
			data.add("IFS:"+ serFilt);
			info.add("nListFilters.size"+nListFilters.size() );}
		if( recComp ){
			info.add( "TOTAL FILTERS BY " + componetName+ " = " + recFilt );
			data.add("IFR:"+recFilt);
			info.add( "nListFilters.size"+nListFilters.size() );
			}
		writeFile( info);
	}
	/**
	 * Function to analyse permissions in component type content provider
	 */
	private static void providers(){
		String expt, permission, readPermission, writePermission;
		boolean perm = false, permR = false, permW = false, exTrue = false, expUndef = false;
		int sdkVer = 0, targetSdk = 0;
		nListProvider = root.getElementsByTagName( "provider" );
		if( nListProvider.getLength() > 0 ){
			for( int i=0; i< nListProvider .getLength(); i++  ){
				provider = (Element) nListProvider.item(i); //tomo cada nodo de la lista
				info.add( "----> Component type: provider"  );
				info.add("Component Name: "+provider.getAttribute("android:name"));
				expt = provider.getAttribute( "android:exported" );
				permission = provider.getAttribute( "android:permission" );
				readPermission = provider.getAttribute( "android:readPermission" );
				writePermission = provider.getAttribute( "android:writePermission" );
				if( ! expt.equalsIgnoreCase("")  ){//exported attribute is defined	//can be exported this provider?
					if( expt.equalsIgnoreCase("true") )
						exTrue = true;
					if( expt.equalsIgnoreCase("false") )
						info.add( providerMsn.get( "exp_false") );
				}else{	//it exported attribute is not defined verifies its default value true or false
						sdkVer = getSdkVertion(0);
						targetSdk = getSdkVertion(1);
						if( sdkVer != 0  &&   targetSdk != 0 ){
							if( (sdkVer <= 16 ||  targetSdk <= 16)  || ( sdkVer >= 17 ||  targetSdk >= 17 ) )
								exTrue = true;
							else 
								info.add( providerMsn.get( "exp_false") );
						}else{
							expUndef = true;
						}
				}
				if( ! permission.equalsIgnoreCase("") )	//permission is defined 
					perm = true;
				if( ! readPermission.equalsIgnoreCase("") )	//permission is defined 
					permR = true;
				if( ! writePermission.equalsIgnoreCase("") )	//permission is defined 
					permW = true;
			
				if( exTrue ){ //exported atribute value true
					if( perm && !permR && !permW ){//if define only one permission
						info.add( providerMsn.get( "exp_true_okpermission" ) );
						info.add( "The permission are: " +  permission );
						data.add("PP");
					}
					if( (!perm) && permR && permW ){ //if define permissions of read and write
						info.add( providerMsn.get( "exp_true_okreadpermission" ) );
						info.add( "The permission are: " +  readPermission );
						info.add( providerMsn.get( "exp_true_okwritepermissions" ) );
						info.add( "The permission are: " +  writePermission );
						data.add("PP");
					}
					if( (!perm) && permR && (!permW) ){ //if only define read permission
						info.add( providerMsn.get( "exp_true_okreadpermission" ) );
						info.add( "The permission are: " +  readPermission );
						data.add("PP");
					}
					if( (!perm) && (!permR) && permW ){ //if only define write permission
						info.add( providerMsn.get( "exp_true_okwritepermissions" ) );
						info.add( "The permission are: " +  writePermission );
						data.add("PP");
					}
					if( (!perm) && (!permR) && (!permW) ) {//exportable without permissions
						info.add( providerMsn.get( "exp_true_notpermission" ) );
					}
				} 
				if( expUndef ){ //exported atribute value is undefined
					if( perm && (!permR) && (!permW) ){//if define only one permission
						info.add( providerMsn.get( "exp_true_okpermission" ) );
						info.add( "The permission are: " +  permission );
						data.add("PP");
					}
					if( (!perm) & permR & permW ){ //if define permissions of read and write
						info.add( providerMsn.get( "exp_true_okreadpermission" ) );
						info.add( "The permission are: " +  readPermission );
						info.add( providerMsn.get( "exp_true_okwritepermissions" ) );
						info.add( "The permission are: " +  writePermission );
						data.add("PP");
					}
					if( (!perm) && (permR) && (!permW) ){ //if only define read permission
						info.add( providerMsn.get( "exp_true_okreadpermission" ) );
						info.add( "The permission are: " +  readPermission );
						data.add("PP");
					}
					if( (!perm) && (!permR) && permW){ //if only define write permission
						info.add( providerMsn.get( "exp_true_okwritepermissions" ) );
						info.add( "The permission are: " +  writePermission );
						data.add("PP");
					}
					if( (!perm) && (!permR) && (!permW) ) {//if only define write permission
						info.add( providerMsn.get( "exp_true_notpermission") );
					}
				}		
			}
			writeFile( info);
		}
	}	
	/**
	 * Function to know minSdkVersion version, targetSdkVersion version, or maxSdkVersion version, defined by te app
	 * if op = 0 search minSdkVersion
	 * if op = 1 search targetSdkVersion
	 * if op = 2 search maxSdkVersion
	 * @return 0 if uses-sdk doesn't defined else uses-sdk version
	 */
	private static int getSdkVertion( int op ){
		int ret = 0;
		sdkList = root.getElementsByTagName( "uses-sdk" );
		if( sdkList.getLength() > 0 ){
			sdk = (Element) sdkList.item(0);
			if( op == 0 )
				ret = Integer.parseInt( sdk.getAttribute("android:minSdkVersion") );
			if( op == 1 )
				ret = Integer.parseInt( sdk.getAttribute("android:targetSdkVersion") );
			if( op == 2 )
				ret = Integer.parseInt( sdk.getAttribute("android:maxSdkVersion") );	
		}else
			ret = 0;	//0 uses-sdk doesn't defined
		
		return ret;
	}
	/**
	 * Function that get all permissions implemented by the developer
	 */
	private static void getNewPermissions(){
		String permLevel;
		newPermissions = root.getElementsByTagName("permission");
		info.add("----> Permissions: " );
		if( newPermissions.getLength() > 0 ){
			data.add("NP:1");
			info.add( permmisionAttrib.get("permissions_true" ) );
			for( int i=0; i<newPermissions.getLength(); i++){
				permission = (Element) newPermissions.item(i); //tomo cada nodo de la lista
				info.add( permission.getAttribute("android:name") ); 
				if( !permission.getAttribute("android:permissionGroup").equalsIgnoreCase("") ){//check if attribute permissionGroup is defined //DEEP!!!!!
					info.add( permmisionAttrib.get("permissionGroup_true") ); 
				}else
					info.add( permmisionAttrib.get("permissionGroup_false") ); 
				permLevel = permission.getAttribute("android:protectionLevel");
				if( !permLevel.equalsIgnoreCase("") ){//check if attribute protectionLevel is defined
					if( ! permLevel.equalsIgnoreCase("") ){	//check permissions levels 
						if( permLevel.equalsIgnoreCase("normal") )
							info.add( permmisionAttrib.get("normal_true") ); 
						if( permLevel.equalsIgnoreCase("dangerous") )
							info.add( permmisionAttrib.get("dangerous_true") );
						if( permLevel.equalsIgnoreCase("signature") )
							info.add( permmisionAttrib.get("signature_true") );
						if( permLevel.equalsIgnoreCase("signatureOrSystem") )
							info.add( permmisionAttrib.get("signatureOrSystem_true") );
					}
				}else
					info.add( permmisionAttrib.get("protectionLevel_false") );
			}
		}else{
			info.add( permmisionAttrib.get("permissions_false" ) );
			 data.add("NP:0");
			}
		
		System.out.println("info_value:"+info.size());
		writeFile(info);
	}
	/**
	 * Function that give all filters of every component and save this on ArrayList attributes
	 */
	public static int setFilters(){
		NodeList tmp;
		int nf = 0;
		ArrayList<NamedNodeMap>attb=new ArrayList<NamedNodeMap>();
		for( int i=0; i< nListFilters.size(); i++  ){
			tmp = nListFilters.get(i);
			for( int t=0; t<tmp.getLength(); t++ )
			{
				Element fieldFilter = (Element) tmp.item(t);
				elementsFilter = fieldFilter.getElementsByTagName("*");
				//System.out.println( "Filter by: "+fieldFilter.getElementsByTagName("*").getLength() );
			}
		}
		
		for( int i=0; i< elementsFilter.getLength(); i++  ){
			attb.add( elementsFilter.item(i).getAttributes() );
		}
		attributes.add( attb );
		return nf = attributes.size();
	}
	/**
	 * Function that give all attributes of ArrayList attributes for every component
	 */
	public static ArrayList<String> getFilters( int pilot, boolean exp, boolean activityComp ){
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<NamedNodeMap>attb=new ArrayList<NamedNodeMap>(), test =new ArrayList<NamedNodeMap>();
		boolean launcher = false,  main = false;
		/*System.out.println("atributes.size: "+ attributes.size());
		for(int i= 0; i<attributes.size(); i++){
			test = attributes.get(i);
			for(int k=0; k< test.size(); k++){
				NamedNodeMap tmp_test = test.get(k);
				Attr attb_test = (Attr)tmp_test.item(0);
				System.out.println( attb_test.getName() + attb_test.getValue());
			}
			
		}*/
		System.out.println("pilotGetFilters: "+ pilot);
		System.out.println("attributes.get(pilot): "+ attributes.get(pilot).size());
			attb = attributes.get(pilot);
			if( ! exp ){//if exp isn't defined check the main activity
				System.out.println("attb.size(): "+attb.size());
				for( int j=0; j< attb.size(); j++ )
				{
					NamedNodeMap tmp = attb.get(j);
					Attr attribute = (Attr) tmp.item(0);
					if( attribute.getValue().equalsIgnoreCase("android.intent.category.LAUNCHER") )
						launcher = true; 
					if( attribute.getValue().equalsIgnoreCase("android.intent.action.MAIN" ))
						main = true;
				}
				if( attb.size() == 2 && activityComp && launcher &&  main ){//if is the main activity
					result.add("main");//is the acti
				}else{			//if not its the main activity
					for( int j=0; j< attb.size(); j++ )//get attributes defined
					{
						NamedNodeMap tmp = attb.get(j);
						Attr attribute = (Attr) tmp.item(0);
						result.add( attribute.getName()+" = "+attribute.getValue() );
						//System.out.println(attribute.getName()+" = "+attribute.getValue());
					}
					System.out.println("intoooooooooo");
				}
				
			}else{	//if not defined attb exported but have intent filters
				for( int j=0; j<= attb.size(); j++ )//get attributes defined
				{
					NamedNodeMap tmp = attb.get(j);
					Attr attribute = (Attr) tmp.item(0);
					result.add( attribute.getName()+" = "+attribute.getValue() );
					//System.out.println(attribute.getName()+" = "+attribute.getValue());
				}
				System.out.println("int11111111111");
			}
			return result;
	}
	/**
	 * Function to write results on and file, It work with Linux directory
	 *
	 */
	public static void writeFile( ArrayList<String> info){
		try
		{	System.out.println("info.length: "+info.size());
			//fileOut = dirOut +"AnalysisResult.txt";  
			fileOut = fileIn.substring( 0, fileIn.lastIndexOf('/')+1 )+"AnalysisResult.txt"; 
			System.out.println("fileOut: "+fileOut );
			PrintWriter p = new PrintWriter( fileOut );
			for( int i=0; i< info.size(); i++ )
			{
				p.println( info.get(i));
				p.flush();
			}
			p.flush();
			p.close();
		}catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public static void writeData( ArrayList<String> info){
		try
		{	System.out.println("info.length: "+info.size());
			fileOut = fileIn.substring( 0, fileIn.lastIndexOf('/')+1 )+"Data.txt"; 
			System.out.println("fileOut: "+fileOut );
			PrintWriter p = new PrintWriter( fileOut );
			for( int i=0; i< info.size(); i++ )
			{
				p.println( info.get(i));
				p.flush();
			}
			p.flush();
			p.close();
		}catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * Function to init methods 
	 */
	 void init(){
		String components[] = { "activity","service","receiver"};//provider requires a different function
		fullingMsnActivity();
		fullingAttbMapT();
		fullingAttbMapF();
		fullingPermissionAtt();
		fullingProviderMsn();
		extractRoot( fileIn);
		info.add("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		//info.add("TOTAL APPS:"+totalApps);
		info.add("APPLICATION NAME: "+getAppName() );
		data.add("APPNAME: "+getAppName());
		
		getNewPermissions();
		providers();
		//info.add(" minSdkVersion: "+ getSdkVertion( 0 ));
		//info.add(" targetSdkVersion: "+ getSdkVertion( 1 ));
		for( int i=0; i<components.length; i++ ){
			components( components[i] );
		}
		/*providers();
		datas.concat("Total Providers: "+nListProvider.getLength()+";");
		writeData(data);*/
		showTotalcomp();
		info.clear();
		data.add("TotalActivities:"+numAct );
		data.add("TotalServices:"+numSer );
		data.add("TotalReceivers:"+numRec );
		data.add("TotalProviders:"+nListProvider.getLength());
		writeData(data);
		data.clear();
		numAct = 0; numSer = 0; numRec = 0; actFilt = 0; serFilt =0; recFilt =0;
		attributes.clear();
//		System.out.println( "Total Activities: "+numAct );
//		System.out.println( "Total Services: "+numSer );
//		System.out.println( "Total Receivers: "+numRec );
//		System.out.println( "Total Providers: "+nListProvider.getLength() );
		
	}
	void showTotalcomp(){
		info.add("Total Activities: "+numAct );
		info.add("Total Services: "+numSer );
		info.add("Total Receivers: "+numRec );
		info.add("Total Providers: "+nListProvider.getLength());
		writeFile(info);
		
	}
	
	/*public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		InputStreamReader lector = new InputStreamReader(System.in); 
		BufferedReader buffer = new BufferedReader( lector );
		System.out.println( "Ingrese el directorio completo del Manifest a analizar" );
		fileIn = buffer.readLine();
		setFileIn( fileIn  );
		setDirout(fileIn.substring( 0, fileIn.lastIndexOf('/')+1 ));
		init();
		
		AppPermissions app_per = new AppPermissions();
		app_per.init();
		app_per.showAllPermss();
	}*/
}