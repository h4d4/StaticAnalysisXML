import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;


public class ReadResult {
	//appsAnalyzed total apps analyzed
	//apppsNP total apps when developer implements at lets new permissions
	//providerP total apps that implemts permissions for its providers
	//activityIF = total apps with IF for its activities
	//serviceIf total apps with IF for its services
	//receiverIf total apps with IF for its receivers
	//apps: # de apps con services; appP: # de apps with providers
	static int appsAnalyzed =0, appsNP = 0, providerP = 0, activityIF = 0, serviceIf = 0, receiverIf = 0;
	static int appS=0, appP=0,appR=0, nAct=0, mainAct=0;
	static boolean pp = false, mAct = false, service = false, receiver = false;
	static File inDir, dirList[];
	static ArrayList<String> files = new ArrayList<String>(), info = new ArrayList<String>();
	private static void ReadPath() throws IOException{
		InputStreamReader lector = new InputStreamReader(System.in); 
		BufferedReader buffer = new BufferedReader( lector );
		System.out.println( "Ingrese el directorio" );
		inDir = new File( buffer.readLine() );
		dirList = inDir.listFiles();
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		            return file.isDirectory();
		    }
		};
		dirList = inDir.listFiles(fileFilter);
		System.out.println(dirList.length);
	      if (dirList.length == 0) {
	          System.out.println("Either dir does not exist or is not a directory");
	       }
	       else {
	          for (int i=0; i< dirList.length; i++) {
	             File filename = dirList[i];
	             System.out.println(filename.toString());
	          }
	       }
		if( inDir != null ){
			for( int i = 0; i < dirList.length; i++ ){
				files.add( inDir+"/"+dirList[i].getName()+"/Data.txt");
			}
		}else
			System.out.println( "No ingreso el directorio completo donde se alojan las aplicaciones a analizar.");
		
		for( int i = 0; i < files.size(); i++ ){
			System.out.println( files.get(i) );
		}
	}
	public static void writeFile( ArrayList<String> info){
		try
		{	System.out.println("info.length: "+info.size());
			String in = inDir.toString(); 
			String fileOut = in.substring(0,in.lastIndexOf('/')+1)+"GeneralizedAnalysis.txt";
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
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	/*	InputStreamReader lector = new InputStreamReader(System.in); 
		BufferedReader buffer1 = new BufferedReader( lector );
		PrintWriter p = new PrintWriter( System.out );
		p.println( "Ingrese el directorio con el archivo" );
		p.flush();
		String fileIn = buffer1.readLine();
		FileInputStream fstream = new FileInputStream(fileIn);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));*/
		String strLine;
		int actIf = 0, servIf=0, recIf=0;
		ReadPath();
		for( int i = 0; i < files.size(); i++ ){
			FileInputStream fstream = new FileInputStream(files.get(i));
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			
			while ( (strLine = br.readLine()) != null )   {
				//System.out.println(strLine);
					if( strLine.contains("APPNAME:")){
						appsAnalyzed += 1;
					}
					if( strLine.contains("NP:")){
						if( strLine.substring( strLine.length()-1,  strLine.length() ).equalsIgnoreCase("1") )
							appsNP +=1;
					}
					if( strLine.contentEquals("PP"))
							pp = true;
					if( strLine.contains("IFA:") )
						actIf = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
					if( strLine.contains("IFS:") )
						servIf = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
					if( strLine.contains("IFR:") )
						recIf = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
					
					if( strLine.contains("TotalActivities:") ){
						int ta = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
						
						if( ta > 1 )	//total activities
							nAct = nAct +1;
						
						if( ta == 1 && actIf == 1 ) //only main activities
							mainAct += 1;
						
						if( ta > 1 &&  actIf>1 )
							activityIF  +=1;
					}
					if( strLine.contains("TotalServices:") ){
						int ts = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
						if( ts>0 )
							appS +=1;
						if( ts >0 && servIf >0 )
							serviceIf +=1;
					}
					if( strLine.contains("TotalReceivers:") ){
						int tr = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
						if( tr>0 )
							appR +=1;
						if( tr>0   && recIf>0 )
							receiverIf += 1;
					}
					if( strLine.contains("TotalProviders:") ){
						int tp = Integer.parseInt( strLine.substring( strLine.length()-1,  strLine.length() ) );
						if( tp>0 )
							appP +=1;
						if( tp>0   && pp )
							providerP += 1;
					}
			}
			br.close();
		}
		
		System.out.println ( "Total apps analyzed: "+appsAnalyzed );
		info.add("Total apps analyzed: "+appsAnalyzed);
		System.out.println ( "Apps when developer defines New permissions: "+appsNP );
		info.add("Apps when developer defines New permissions: "+appsNP);
		System.out.println ( "Apps that implemets Providers: "+appP);
		info.add("Apps that implemets Providers: "+appP);
		System.out.println ( "Permissions Providers: "+providerP );
		info.add("Permissions Providers: "+providerP);
		System.out.println ( "Apps that implements activities: "+nAct );
		info.add("Apps that implements activities: "+nAct);
		System.out.println ( "Apps that implements only mainActivities: "+mainAct );
		info.add("Apps that implements only mainActivities: "+mainAct);
		System.out.println ( "Apps with at least one IF to its Activities: "+activityIF );
		info.add("Apps with at least one IF to its Activities: "+activityIF);
		System.out.println ( "Apps that implemets Services: "+appS);
		info.add("Apps that implemets Services: "+appS);
		System.out.println ( "Apps with at least one IF to its Services: "+serviceIf );
		info.add("Apps with at least one IF to its Services: "+serviceIf);
		System.out.println ( "Apps that implemets Receivers: "+appR);
		info.add("Apps that implemets Receivers: "+appR);
		System.out.println ( "Apps with at least one IF to its Receivers: "+receiverIf );
		info.add("Apps with at least one IF to its Receivers: "+receiverIf);
		writeFile(info);
	}

}
