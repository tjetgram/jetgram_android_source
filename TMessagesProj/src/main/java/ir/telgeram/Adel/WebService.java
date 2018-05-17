package ir.telgeram.Adel;

import android.os.AsyncTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class WebService extends AsyncTask<String, Integer, String>
{
	private final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
	private final String SOAP_ADDRESS          = "http://telgramfarsi.ir/WebService.asmx";

	public String Function = null;
	public Object mListener;

	public WebService(Object listener)
	{
		mListener = listener;
	}

	// ---------------- Zangooleh -----------------
	public String ZangoolehList()
	{
		String OPERATION = "ZangoolehList";
		String ADDRESS   = "http://tempuri.org/ZangoolehList";

		SoapObject                request  = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;

		envelope.setOutputSoapObject(request);

		HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
		Object          response      = null;
		try
		{
			httpTransport.call(ADDRESS, envelope);
			response = envelope.getResponse();
		} catch (Exception exception)
		{
			response = BaseApplication.ANDROID_EXCEPTION;
		}
		return response.toString();
	}

	public String GetLastZangoolehId()
	{
		String OPERATION = "GetLastZangoolehId";
		String ADDRESS   = "http://tempuri.org/GetLastZangoolehId";

		SoapObject                request  = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;

		envelope.setOutputSoapObject(request);

		HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
		Object          response      = null;
		try
		{
			httpTransport.call(ADDRESS, envelope);
			response = envelope.getResponse();
		} catch (Exception exception)
		{
			response = BaseApplication.ANDROID_EXCEPTION;
		}
		return response.toString();
	}

	// --------------------------------------------------------------------------------
	protected String doInBackground(String... in)
	{
		Function = in[0];

		// --- Zangooleh ---
		if (Function.equals("ZangoolehList"))
		{
			return ZangoolehList();
		}

		if (Function.equals("GetLastZangoolehId"))
		{
			return GetLastZangoolehId();
		}

		return "false";
	}

	protected void onPostExecute(String response)
	{
		// --- Zangooleh ---
		if (Function.equals("ZangoolehList"))
		{
			((IZangoolehList) mListener).onZangoolehListCompleted(response);
		}
		else if (Function.equals("GetLastZangoolehId"))
		{
			((IGetLastZangoolehId) mListener).onGetLastZangoolehIdCompleted(response);
		}
	}
}