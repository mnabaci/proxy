package com.android.proxy.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Request implements Parcelable {
	
	public static final int ACTION_GET = 1;
	public static final int ACTION_POST = 2;
	public static final int ACTION_DELETE = 3;
	public static final int ACTION_PUT = 4;
	
	public int action;
	public String packageName;
	public String items;
	public String versionId;
	public String flatId;
	public String objectId;
	public List<Map> objects;
	
	public Request() {
		objects = new ArrayList<Map>();
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeInt(action);
		arg0.writeString(packageName);
		arg0.writeString(items);
		arg0.writeString(versionId);
		arg0.writeString(flatId);
		arg0.writeString(objectId);
		arg0.writeList(objects);
	}
	
	public void readFromParcel(Parcel in) {
		action = in.readInt();
		packageName = in.readString();
		items = in.readString();
		versionId = in.readString();
		flatId = in.readString();
		objectId = in.readString();
		Log.d("Request", "before read list");
		if (objects == null) {
			objects = new ArrayList<Map>();
		}
		in.readList(objects, getClass().getClassLoader());
	}

	
	public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
		public Request createFromParcel(Parcel in) {
		    return new Request(in);
		}

		public Request[] newArray(int size) {
		    return new Request[size];
		}
	};

	private Request(Parcel in) {
        readFromParcel(in);
    }
	
	private static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;
	
	private static TransformerFactory tFactory;
    private static Transformer transformer;
    
    static {
    	factory = DocumentBuilderFactory.newInstance();
    	try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	tFactory = TransformerFactory.newInstance();
        try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public static void transformObjectsToXML(Request request, String file) {
		List<Map> list = request.objects;
		Document doc = builder.newDocument();
		Element root = doc.createElement("objects");
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				Object[] keys = map.keySet().toArray();
				Element object = doc.createElement("object");
				for (int j = 0; j < keys.length; j++) {
					Element attribute = doc.createElement(keys[j].toString());
					Text value = doc.createTextNode(map.get(keys[j]).toString());
					attribute.appendChild(value);
					object.appendChild(attribute);
				}
				root.appendChild(object);
			}
		}
		doc.appendChild(root);
		
		DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new File(file));
	    try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
