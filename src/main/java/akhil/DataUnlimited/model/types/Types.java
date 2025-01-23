package akhil.DataUnlimited.model.types;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mariuszgromada.math.mxparser.License;

import akhil.DataUnlimited.ui.DBConfig;
import akhil.DataUnlimited.ui.UI;
import akhil.DataUnlimited.model.parameter.DataGenerator;
import akhil.DataUnlimited.model.parameter.ParamFunctionDefs;
import akhil.DataUnlimited.model.parameter.ParamFunctions;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.FileOperation;

public class Types {
	private static final Logger lo = LogManager.getLogger(Types.class.getName());
	// Most of the keywords are defined in this class, with color coding on the UI
	// and adding parameters so that UI can take it.
	public static final String GROUPSTARTDELIMITER = "#GrOuP#";
	public static final String GROUPENDDELIMITER = "#/GrOuP#";
	public static final String SEGMENTENDDELIMITER = "#/SeGmEnT#";
	public static final String SEGMENTSTARTDELIMITER = "#SeGmEnT#";
	public static final String CONTENTTYPESEGMENT = "segment";
	public static final String CONTENTTYPEGROUP = "group";
	public static final String SEGMENTCONTENTCONSTANT = "constant";
	public static final String SEGMENTCONTENTPARAM = "param";
	public static final String DATETYPEPAST = "past";
	public static final String DATETYPEFUTURE = "future";
	public static final String DATEFORMATEPOCH = "epoch";
	public static final String DMSEXAMPLESTART = "<dms-example>";
	public static final String DMSEXAMPLENAMESTART = "<dms-example-name>";
	public static final String DMSEXAMPLENAMEEND = "</dms-example-name>";
	public static final String DMSEXAMPLEEND = "</dms-example>";
	public static final String ITERATIONPOLICYGLOBAL = "global";
	public static final String ITERATIONPOLICYGROUP = "group";
	public static final Color FINDCOLOR = Color.YELLOW;
	public static final Color DEFAULTBACKGROUNDCOLOR = Color.WHITE;
	public static final String DTDSCHEMAPCDATA = "PCDATA";
	public static final String DTDSCHEMAELEMENT = "ELEMENT";
	public static final String CUSTOMPARAMNAMEENCLOSURE = "#";
	public static final String DMSCOMMENTSTART = "<dms-comment>";
	public static final String DMSCOMMENTEND = "</dms-comment>";
	public static final String DMSCOMMENTSTARTOLD = "<DMSCOMMENT>";
	public static final String DMSCOMMENTENDOLD = "</DMSCOMMENT>";
	public static final String DMSDELIM = "<DMSDELIM>";
	public static final String DMSSUBDELIM = "<DMSSUBDELIM>";
	public static final String DMSCONFIGITEMDELIM = "<DMSCONFIGITEMDELIM>";
	public static final String DMSCONFIGCOMMENTSTART = "<DMSCONFIGCOMMENT>";
	public static final String DMSCONFIGCOMMENTEND = "</DMSCONFIGCOMMENT>";
	public static final String PAYLOADSTART = "<xs:MessagePayload>";
	public static final String PAYLOADEND = "</xs:MessagePayload>";
	public static final String DMSSPACE = "#DMSSPACE#";
	public static final String DMSNULL = "#DMSNULL#";
	public static final String DMSEXTRACTMAPPERSTART = "<dms-extmapper>";
	public static final String DMSEXTRACTMAPPEREND = "</dms-extmapper>";
	public static final String DMSNULLLOWER = "#dmsnull#";
	public static final String DMSCMDLINENAMEVALUESEPARATOR = "###";
	public static final String DMSCMDLINEREGEXVALUESEPARATOR = "###";
	public static final String DMSBACKSLASH = "#DMSBACKSLASH#";
	public static final String PARAMSTARTSHOW = "{#show#{";
	public static final String PARAMSTARTHIDE = "{#hide#{";
	public static final String PARAMSTARTSHOWLOG = "{#showlog#{";
	public static final String PARAMSTARTHIDELOG = "{#hidelog#{";
	public static final String PARAMEND = "}}";
	public static final String DMSEXTRACTINPUTDATASTART = "<dms-extinputdata>";
	public static final String DMSEXTRACTINPUTDATAEND = "</dms-extinputdata>";

	public static String DBURL = "jdbc:sqlite:../db/DMSV.db";
	public static String DBCLASS = "org.sqlite.JDBC";
	public static String DBUSR = "postgres";
	public static String DBPWD = "postgres";

	public static void setDBURL(String s) {
		DBURL = s;
	}

	public static void setDBCLASS(String s) {
		DBCLASS = s;
	}

	public static void setDBUSR(String s) {
		DBUSR = s;
	}

	public static void setDBPWD(String s) {
		DBPWD = s;
	}

	private static final String[] DTDSCHEMACHARSTOREPLACEPLUS = { "+", "*", "?" };

	public static String[] getDTDSchemaCharStorePlacePlus() {
		return DTDSCHEMACHARSTOREPLACEPLUS;
	}

	public static final String DTDSCHEMAMESSAGE = "File will be loaded as DTD schema. Original XML can be used to generate DTD schema via tool such as XML Copy Editor."
			+ "Lines containing text \"ELEMENT\" and \"PCDATA\" will be processed to create script, based on how different tags are nested."
			+ "You may choose to create a template with empty XML tags or load the values from original XML (from which schema was generated)."
			+ "To preload original XML value, you will be asked to select original XML file after schema file is chosen and loaded."
			+ "Please note that text boxes used for FIND and REPLACE above will be \"REPURPOSED\".\n In case, file has unwanted lines/strings/characters:"
			+ "\n\n \t 1. Use FIND text box to enter comma separated strings. Lines containing these strings will be removed from the content."
			+ "\n\n \t 2. Use REPLACE text box to enter comma separated strings or characters."
			+ " These strings and characters will be deleted from the lines.\n\nPlease note you don't have to use Find/Replace buttons during the process.\n"
			+ "Once the basic template is generate, please parameterize the template appropriately and save it for future data generation needs.";
	private static List<String> customparamdirs = new ArrayList<>();
	private UI ui;
	private String[] paramTypes;
	private static Types type;
	private String[] elementTypes;

	public String[] getElementTypes() {
		return elementTypes;
	}

	public Color getFindColor() {
		return FINDCOLOR;
	}

	public Color getDefaultBackgroundColor() {
		return DEFAULTBACKGROUNDCOLOR;
	}

	private Map<String, Color> keywordsAndColors;

	public Map<String, Color> getKeywordsAndColors() {
		return keywordsAndColors;
	}

	private Map<String, String> paramTypeAndSampleMap;

	public Map<String, String> getParamTypeAndSampleMap() {
		return paramTypeAndSampleMap;
	}

	private Map<String, String> elementTypeAndSampleMap;

	public Map<String, String> getElementTypeAndSampleMap() {
		return elementTypeAndSampleMap;
	}

	private static Map<String, ParamFunctions> paramFunctionMap = new HashMap<>();

	public static Map<String, ParamFunctions> getParamFunctionMap() {
		return paramFunctionMap;
	}

	public static boolean checkCustomParamDirExists(String dir) {
		return customparamdirs.contains(dir);
	}

	public static Types getInstance() {
		if (type == null) {
			type = new Types();
		}
		return type;
	}

	public String[] getDataTypeList() {
		String[] sa = new String[DataGenerator.getInstance().getData().size()
				+ DataGenerator.getInstance().getParamData().size()];
		int count = 0;
		for (String s : DataGenerator.getInstance().getData().keySet()) {
			sa[count] = s;
			count++;
		}
		for (String s : DataGenerator.getInstance().getParamData().keySet()) {
			sa[count] = s;
			count++;
		}
		return sa;
	}

	private DBConfig dbc;
	private boolean isUI = false;

	public void setIsUI(boolean tf) {
		this.isUI = tf;
	}

	public boolean getIsUI() {
		return isUI;
	}

	public void setUI(UI ui) {
		this.ui = ui;
		setIsUI(true);
	}

	public UI getUI() {
		return ui;
	}

	public void setDBConfig(DBConfig dbc) {
		this.dbc = dbc;
	}

	public DBConfig getDBConfig() {
		return dbc;
	}

	private static Map<String, Color> outputColors = new HashMap<>();

	public static Map<String, Color> getOutputColors() {
		return outputColors;
	}

	public static void addLogColor(String s, int c) {

		if (c == 200)
			outputColors.put(s, Color.RED);
		if (c == 400)
			outputColors.put(s, new Color(0, 153, 0));
		if (c == 300)
			outputColors.put(s, Color.ORANGE);

	}

	public static void clearLogColor() {
		outputColors.clear();
	}

	public static final String GROUPANDSEGMENT = "Group & Segment";
	public static final String GROUP = "Group";
	public static final String SEGMENT = "Segment";
	public static final String COMMENT = "Comment";
	public static final String VFP = "Virtual Param File";
	public static final String EID = "Extract Input Data";
	public static final String EXTMAP = "Extract Mapper";
	public static final String GVFP = "Global Virtual Param File";
	public static final String RN = "randomNumber";
	public static final String TF = "tightfile";
	public static final String VF = "virtualfile";
	public static final String MPF = "multiParamFile";
	public static final String SQ = "dbQuery";
	public static final String ST = "SQLite Table";
	public static final String STF = "SQLite Table File";
	public static final String DB = "db";
	public static final String FILE = "file";
	public static final String EVAL = "eval";
	public static final String P = "primary";
	public static final String S = "secondary";
	public static final String UN = "uniqueNumber";
	public static final String RNDDT = "randomDateTime";
	public static final String GI = "groupIteration";
	public static final String RELDT = "relativeDateTime";
	public static final String NS = "numberSequence";
	public static final String RUS = "randomUpperCaseString";
	public static final String RLS = "randomLowerCaseString";
	public static final String RMS = "randomMixedCaseString";
	public static final String RRN = "rangeRandomNumber";
	public static final String RAS = "randomAlphaNumericString";
	public static final String NSR = "numberSequenceRange";
	public static final String RNGDT = "rangeDateTime";
	public static final String A = "array";
	public static final String CDT = "customDataType";
	public static final String W = "where";
	public static final String WL = "wherelike";
	public static final String UUID = "uuid";
	public static final String DBS = "dbStore";
	public static final String STR = "embeddedParamString";
	public static final String DCD = "decode";
	public static final String MCL = "mathCalc";
	
	
	private Types() {
		elementTypes = new String[] { GROUPANDSEGMENT, GROUP, SEGMENT, COMMENT, VFP, ST, STF, EID, EXTMAP, GVFP };
		elementTypeAndSampleMap = new HashMap<>();
		elementTypeAndSampleMap.put(GROUPANDSEGMENT, "#GrOuP#groupName#1-1^^\n#SeGmEnT#1^^\n\n#/SeGmEnT#\n#/GrOuP#");
		elementTypeAndSampleMap.put(GROUP, "#GrOuP#groupName#1-1^^\n\n#/GrOuP#");
		elementTypeAndSampleMap.put(SEGMENT, "#SeGmEnT#1^^\n\n#/SeGmEnT#");
		elementTypeAndSampleMap.put(COMMENT, DMSCOMMENTSTART + "\n\n" + DMSCOMMENTEND);
		elementTypeAndSampleMap.put(EID, DMSEXTRACTINPUTDATASTART + "\n\n" + DMSEXTRACTINPUTDATAEND + "\n");
		elementTypeAndSampleMap.put(EXTMAP, DMSEXTRACTMAPPERSTART + "\n\n" + DMSEXTRACTMAPPEREND + "\n");
		elementTypeAndSampleMap.put(VFP,
				"<dms-vf>\n<dms-vfname><VIRTUALFILENAME>.txt</dms-vfname>\n<dms-vfdata>\n\n</dms-vfdata>\n</dms-vf>\n");
		elementTypeAndSampleMap.put(ST,
				"<dms-sqlite-table>\n<dms-tablename>TESTTABLE</dms-tablename>\n<dms-delimiter>,</dms-delimiter>\n<dms-columnnames>COL1,COL2</dms-columnnames>\n<dms-tabledata>\nCOL1DATA,COL2DATA\n</dms-tabledata>\n</dms-sqlite-table>\n");
		elementTypeAndSampleMap.put(STF, "<sqlite-table-data-filepath>\n\n</sqlite-table-data-filepath>\n");
		elementTypeAndSampleMap.put(GVFP,
				"<dms-g-vf>\n<dms-vfname><GLOBALVIRTUALFILENAME>.txt</dms-vfname>\n<dms-vfdata>\n\n</dms-vfdata>\n</dms-g-vf>\n");

		keywordsAndColors = new TreeMap<>();
		keywordsAndColors.put(GROUPSTARTDELIMITER, Color.BLUE);
		keywordsAndColors.put(GROUP, Color.BLUE);
		keywordsAndColors.put(GROUPANDSEGMENT, Color.BLUE);

		keywordsAndColors.put(DB, Color.RED);
		keywordsAndColors.put(FILE, Color.RED);

		keywordsAndColors.put(VFP, new Color(51, 153, 255));
		keywordsAndColors.put(ST, new Color(51, 153, 255));
		keywordsAndColors.put(STF, new Color(51, 153, 255));
		keywordsAndColors.put(GVFP, new Color(51, 153, 255));
		keywordsAndColors.put(EID, new Color(51, 153, 180));
		keywordsAndColors.put(EXTMAP, new Color(51, 153, 255));
		keywordsAndColors.put("<dms-g-vf>", new Color(51, 180, 205));
		keywordsAndColors.put("</dms-g-vf>", new Color(51, 180, 205));
		keywordsAndColors.put("<dms-global-virtualfile>", new Color(51, 180, 205));
		keywordsAndColors.put("</dms-global-virtualfile>", new Color(51, 180, 205));
		keywordsAndColors.put("<dms-vf>", new Color(51, 153, 255));
		keywordsAndColors.put("</dms-vf>", new Color(51, 153, 255));
		keywordsAndColors.put("<dms-virtualfile>", new Color(51, 153, 255));
		keywordsAndColors.put("</dms-virtualfile>", new Color(51, 153, 255));
		keywordsAndColors.put("<dms-virtualfile>", new Color(51, 153, 255));
		keywordsAndColors.put("<dms-sqlite-table>", new Color(51, 153, 255));
		keywordsAndColors.put("</dms-sqlite-table>", new Color(51, 153, 255));
		keywordsAndColors.put("<sqlite-table-data-filepath>", new Color(51, 153, 255));
		keywordsAndColors.put("</sqlite-table-data-filepath>", new Color(51, 153, 255));
		keywordsAndColors.put("<dms-vfname>", new Color(51, 153, 180));
		keywordsAndColors.put("</dms-vfname>", new Color(51, 153, 180));
		keywordsAndColors.put("<dms-tablename>", new Color(51, 153, 180));
		keywordsAndColors.put("</dms-tablename>", new Color(51, 153, 180));
		keywordsAndColors.put("<dms-delimiter>", new Color(51, 153, 180));
		keywordsAndColors.put("</dms-delimiter>", new Color(51, 153, 180));
		keywordsAndColors.put("<dms-columnnames>", new Color(51, 153, 180));
		keywordsAndColors.put("</dms-columnnames>", new Color(51, 153, 180));
		keywordsAndColors.put("<dms-virtualfilename>", new Color(51, 153, 180));
		keywordsAndColors.put("</dms-virtualfilename>", new Color(51, 153, 180));
		keywordsAndColors.put("<dms-vfdata>", new Color(51, 200, 180));
		keywordsAndColors.put("</dms-vfdata>", new Color(51, 200, 180));
		keywordsAndColors.put("<dms-tabledata>", new Color(51, 200, 180));
		keywordsAndColors.put("</dms-tabledata>", new Color(51, 200, 180));
		keywordsAndColors.put("<dms-virtualfiledata>", new Color(51, 200, 180));
		keywordsAndColors.put("</dms-virtualfiledat>", new Color(51, 200, 180));
		keywordsAndColors.put(DMSEXTRACTINPUTDATASTART, new Color(51, 153, 180));
		keywordsAndColors.put(DMSEXTRACTINPUTDATAEND, new Color(51, 153, 180));
		keywordsAndColors.put("groupName", Color.BLUE);
		keywordsAndColors.put(GROUPENDDELIMITER, Color.BLUE);
		keywordsAndColors.put(SEGMENTENDDELIMITER, new Color(206, 125, 42));
		keywordsAndColors.put(SEGMENT, new Color(206, 125, 42));
		keywordsAndColors.put(SEGMENTSTARTDELIMITER, new Color(206, 125, 42));
		keywordsAndColors.put("^", Color.RED);
		keywordsAndColors.put(PARAMSTARTSHOW, Color.RED);
		keywordsAndColors.put(PARAMSTARTHIDE, Color.RED);
		keywordsAndColors.put(PARAMSTARTSHOWLOG, Color.RED);
		keywordsAndColors.put(PARAMSTARTHIDELOG, Color.RED);
		keywordsAndColors.put("}}", Color.RED);
		keywordsAndColors.put("{{", Color.RED);
		keywordsAndColors.put(DMSEXTRACTMAPPERSTART, new Color(51, 153, 255));
		keywordsAndColors.put(DMSEXTRACTMAPPEREND, new Color(51, 153, 255));
		keywordsAndColors.put(EVAL, Color.RED);
		keywordsAndColors.put("node", new Color(153, 0, 0));
		keywordsAndColors.put("extract", new Color(153, 0, 0));
		keywordsAndColors.put(DMSDELIM, new Color(206, 125, 42));

		keywordsAndColors.put(RN, Color.RED);
		keywordsAndColors.put(TF, Color.RED);
		keywordsAndColors.put(VF, Color.RED);
		keywordsAndColors.put(MPF, Color.RED);
		keywordsAndColors.put(SQ, Color.RED);
		keywordsAndColors.put(P, Color.RED);
		keywordsAndColors.put(S, Color.RED);
		keywordsAndColors.put(UN, Color.RED);
		keywordsAndColors.put(RNDDT, Color.RED);
		keywordsAndColors.put(GI, Color.RED);
		keywordsAndColors.put(RELDT, Color.RED);
		keywordsAndColors.put(NS, Color.RED);
		keywordsAndColors.put(RUS, Color.RED);
		keywordsAndColors.put(RLS, Color.RED);
		keywordsAndColors.put(RMS, Color.RED);
		keywordsAndColors.put(EVAL, Color.RED);
		keywordsAndColors.put(RRN, Color.RED);
		keywordsAndColors.put(RAS, Color.RED);
		keywordsAndColors.put(NSR, Color.RED);
		keywordsAndColors.put(RNGDT, Color.RED);
		keywordsAndColors.put(UUID, Color.RED);
		keywordsAndColors.put(DBS, Color.RED);
		keywordsAndColors.put(DCD, Color.RED);
		keywordsAndColors.put(MCL, Color.RED);
		keywordsAndColors.put(STR, Color.RED);
		keywordsAndColors.put(A, Color.RED);
		keywordsAndColors.put(CDT, Color.RED);
		keywordsAndColors.put("jdbc:sqlite:", Color.RED);

		keywordsAndColors.put(ITERATIONPOLICYGLOBAL, Color.RED);
		keywordsAndColors.put(CONTENTTYPEGROUP, Color.RED);
		keywordsAndColors.put("eachOccurence", Color.RED);
		keywordsAndColors.put("true", Color.RED);
		keywordsAndColors.put("false", Color.RED);
		keywordsAndColors.put("upper", Color.RED);
		keywordsAndColors.put("lower", Color.RED);
		keywordsAndColors.put("asis", Color.RED);
		keywordsAndColors.put(W, Color.RED);
		keywordsAndColors.put(WL, Color.RED);

		keywordsAndColors.put(DMSCOMMENTSTART, new Color(225, 0, 255));
		keywordsAndColors.put(DMSCOMMENTEND, new Color(225, 0, 255));
		keywordsAndColors.put(DMSCOMMENTSTARTOLD, new Color(225, 0, 255));
		keywordsAndColors.put(DMSCOMMENTENDOLD, new Color(225, 0, 255));
		keywordsAndColors.put(COMMENT, new Color(225, 0, 255));
		keywordsAndColors.put("<VIRTUALFILENAME>.txt", Color.ORANGE);

		paramTypes = new String[] { A, DB, FILE, GI, NS, RAS, RNDDT, RLS, RMS, RN, RUS, RNGDT, RRN, RELDT, UN, CDT, NSR,
				TF, EVAL, VF, MPF, SQ, W, WL, UUID, DBS, STR, DCD, MCL };

		paramTypeAndSampleMap = new TreeMap<>();
		paramTypeAndSampleMap.put(RN,
				"{#show#{parameterName^randomNumber^substitutionPolicy-global/eachOccurence/group^num of digits- can be range like 5-9 or 9}}");
		paramTypeAndSampleMap.put(NS,
				"{#show#{parameterName^numberSequence^substitutionPolicy-global/eachOccurence/group^num of digits- can be range like 5-9 or 9^0 padding - false/true^reset in each file - true/false}}");
		paramTypeAndSampleMap.put(NSR,
				"{#show#{parameterName^numberSequenceRange^substitutionPolicy-global/eachOccurence/group^num of digits- can be range like 5-9 or 9^0 padding - false/true^reset in each file - true/false^lower limit^upper limit (0 for no limit)}}");
		paramTypeAndSampleMap.put(UN,
				"{#show#{parameterName^uniqueNumber^substitutionPolicy-global/eachOccurence/group^num of digits- can be range like 5-9 or 9}}");
		paramTypeAndSampleMap.put(RNDDT,
				"{#show#{parameterName^randomDateTime^substitutionPolicy-global/eachOccurence/group^format - epoch/SimpleDateTime (Use Help Menu)^Approx 3.5 months from system time past/future, choose from - past/future/systemTime}}");
		paramTypeAndSampleMap.put(RELDT,
				"{#show#{parameterName^relativeDateTime^substitutionPolicy-global/eachOccurence/group^format - epoch/SimpleDateTime (Use Help Menu)^Days (e.g. 1.234) from system Time(+/-)}}");
		paramTypeAndSampleMap.put(RAS,
				"{#show#{parameterName^randomAlphaNumericString^substitutionPolicy-global/eachOccurence/group^num of characters (can be range or single (5-9 or 9)}}");
		paramTypeAndSampleMap.put(RUS,
				"{#show#{parameterName^randomUpperCaseString^substitutionPolicy-global/eachOccurence/group^num of characters- can be range or single (5-9 or 9)}}");
		paramTypeAndSampleMap.put(RLS,
				"{#show#{parameterName^randomLowerCaseString^substitutionPolicy-global/eachOccurence/group^num of characters- can be range or single (5-9 or 9)}}");
		paramTypeAndSampleMap.put(RMS,
				"{#show#{parameterName^randomMixedCaseString^substitutionPolicy-global/eachOccurence/group^num of characters- can be range or single (5-9 or 9)}}");
		paramTypeAndSampleMap.put(FILE,
				"{#show#{parameterName^file^substitutionPolicy-global/eachOccurence/group^filePath^columnNumber^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^delimiter e.g. ,^randomize true/false^reset in each file - true/false^(optional) row multiplier - format 1x2_3x4_6x3 etc^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(TF,
				"{#show#{parameterName^tightfile^substitutionPolicy-global/eachOccurence/group^primary/(use eachOccurence)secondary^filePath^columnNumber^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^delimiter e.g. ,^randomize true/false^reset in each file - true/false^(optional) row multiplier - format 1x2_3x4_6x3 etc^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(A,
				"{#show#{parameterName^array^substitutionPolicy-global/eachOccurence/group^Delimiter Separated Values^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^delimiter e.g. ,^randomize true/false^reset in each file - true/false}}");
		paramTypeAndSampleMap.put(GI,
				"{#show#{parameterName^groupIteration^eachOccurence^num of digits- can be range like 5-9 or 9, & 18+ 0 padded^0 padding - false/true}}");
		paramTypeAndSampleMap.put(DB,
				"{#show#{parameterName^db^substitutionPolicy-global/eachOccurence/group^db config file path^columnNumber^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^randomize true/false^reset in each file - true/false^Print Query Result to File true/false^(optional) FIND<DMSDELIM>REPLACE in config file (repeat N times)^(optional) output file name for printing query result to file}}");
		paramTypeAndSampleMap.put(RNGDT,
				"{#show#{parameterName^rangeDateTime^substitutionPolicy-global/eachOccurence/group^format - epoch/SimpleDateTime (Use Help Menu)^lower date limit (yyyy-MM-dd hh:mm:ss - 24 hour format)^upper lower date limit (yyyy-MM-dd hh:mm:ss - 24 hour format)}}");
		paramTypeAndSampleMap.put(RRN,
				"{#show#{parameterName^rangeRandomNumber^substitutionPolicy-global/eachOccurence/group^num of digits- can be range like 5-9 or 9, doesn't matter if 0 padding is false^0 padding - false/true^lower limit (min -> 0)^upper limit (max -> 9223372036854775806)}}");
		paramTypeAndSampleMap.put(CDT,
				"{#show#{parameterName^customDataType^substitutionPolicy-global/eachOccurence/group^format - choose from available data types (from UI) and text/space around^change case - upper/lower/none (optional)}}");
		paramTypeAndSampleMap.put(EVAL,
				"{#show#{parameterName^eval^substitutionPolicy-global/eachOccurence/group^[[expression to eval]]^(optional) initial value}}");
		paramTypeAndSampleMap.put(VF,
				"{#show#{parameterName^virtualfile^substitutionPolicy-global/eachOccurence/group^virtualFileName^columnNumber^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^delimiter e.g. ,^randomize true/false^reset in each file - true/false^(optional) row multiplier - format 1x2_3x4_6x3 etc^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(MPF,
				"{#show#{parameterName^multiParamFile^substitutionPolicy-global/eachOccurence/group^filePath^virtualFileSectionName^columnNumber^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^delimiter e.g. ,^randomize true/false^reset in each file - true/false^(optional) row multiplier - format 1x2_3x4_6x3 etc^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(SQ,
				"{#show#{parameterName^dbQuery^substitutionPolicy-global/eachOccurence/group^select query^startFrom(1 for starting from start)^resetAfterNumber(0 for no limit)^randomize true/false^reset in each file - true/false}}");
		paramTypeAndSampleMap.put(W,
				"{#show#{parameterName^where^substitutionPolicy-global/eachOccurence/group^file/virtualfile^filepath/virtualFileName^(optional for file) virtualFileSectionName^columnNumber^1^0^delimiter e.g. ,^false^false^filterColumnNumber^filterColumnValue^isFilterValueParam(true/false)^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(WL,
				"{#show#{parameterName^wherelike^substitutionPolicy-global/eachOccurence/group^file/virtualfile^filepath/virtualFileName^(optional for file) virtualFileSectionName^columnNumber^1^0^delimiter e.g. ,^false^false^filterColumnNumber^filterColumnValue^isFilterValueParam(true/false)^returnCountOnly-true/false^returnSorted-true/false^returnUnique-true/false}}");
		paramTypeAndSampleMap.put(UUID, "{#show#{parameterName^uuid^substitutionPolicy-global/eachOccurence/group}}");

		paramTypeAndSampleMap.put(DBS, "{#show#{parameterName^dbStore^substitutionPolicy-global/eachOccurence/group^insert/update query [insert into tablename (col1, col1) values ('param1', 'param2')]/[update tablename set col1 = 'param1', col2 = 'param2' where col3 = 'param3'] - don't use exact parameter name as column names}}");

		paramTypeAndSampleMap.put(STR, "{#show#{parameterName^embeddedParamString^substitutionPolicy-global/eachOccurence/group^String with parameterName(s) embedded}}");
		
		paramTypeAndSampleMap.put(DCD, "{#show#{parameterName^decode^substitutionPolicy-global/eachOccurence/group^parameterName to decode^Decode String - \"data1\", \"value1\",\"data2\", \"value2\", (optional)\"default\", (optional)\"default value\"}}");
		
		paramTypeAndSampleMap.put(MCL, "{#show#{parameterName^mathCalc^substitutionPolicy-global/eachOccurence/group^[[ math expression to calculate ]]^Decimal Format 0.#^(optional) initial value}}");
		
		
		new ParamFunctionDefs().defineParamFunctions(paramFunctionMap);

	}

	static boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
	static boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

	public static boolean isWindows() {
		return isWin;
	}

	public static boolean isMacOS() {
		return isMac;
	}

	public String[] getParamTypes() {
		return paramTypes;
	}

	public void init(String datalib) {
		createDataTypeList(datalib, ".dat");
		for (String s : DataGenerator.getInstance().getData().keySet())
			keywordsAndColors.put(s, Color.MAGENTA);
		for (String s : DataGenerator.getInstance().getParamData().keySet())
			keywordsAndColors.put(s, Color.MAGENTA);
		
		License.iConfirmCommercialUse("Akhil Sharma");
		License.checkIfUseTypeConfirmed();

	}

	private void createDataTypeList(String dir, String ext) {
		if (ui != null && getIsUI())
			DULogger.log(400, "INFO: Loading data files (*." + ext + ") for custom data type parameters from " + dir
					+ " directory...");
		File[] datfiles = FileOperation.getListofFiles(dir, ext);
		if (datfiles != null) {
			for (File f : datfiles) {
				List<String> data = FileOperation.getContentAsList(f.getAbsolutePath(), "utf8");
				for (String s : data) {
					if (s.length() > 0 && !s.startsWith("//") && !s.startsWith("#")) // "//" or "#" in data file can be
																						// used for comments
					{
						String name = s.substring(0, s.indexOf('=')).trim();
						DataGenerator.getInstance().ingestDataFromDat("#" + name + "#", s.substring(s.indexOf('=') + 1)
								.replace("<filepath>", f.getParentFile().getAbsolutePath()));
					}
				}
			}
			customparamdirs.add(dir);
		} else {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(300,
						"WARNING: Directory for custom parameters datalib wasn't found. CustomDataTypeParam won't work until datalib is loaded. If you are using UI, you can load it from there too.");
			} else
				lo.info("WARNING: Directory for custom parameters datalib wasn't found. CustomDataTypeParam won't work until datalib is loaded. If you are using UI, you can load it from there too.");
		}
	}
}
