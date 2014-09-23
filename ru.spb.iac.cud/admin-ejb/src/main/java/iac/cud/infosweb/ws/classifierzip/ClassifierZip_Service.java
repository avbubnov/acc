package iac.cud.infosweb.ws.classifierzip;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.4
 * 2014-06-03T17:51:56.155+04:00
 * Generated source version: 2.6.4
 * 
 */
@WebServiceClient(name = "ClassifierZip", 
                  wsdlLocation = "http://192.168.2.173:80/WSclassif/ClassifierZip?wsdl",
                  targetNamespace = "http://ws.iac.spb.ru/ClassifierZip") 
public class ClassifierZip_Service extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://ws.iac.spb.ru/ClassifierZip", "ClassifierZip");
    public final static QName ClassifierZipPort = new QName("http://ws.iac.spb.ru/ClassifierZip", "ClassifierZipPort");
    static {
        URL url = null;
        try {
            url = new URL("http://192.168.2.173:80/WSclassif/ClassifierZip?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(ClassifierZip_Service.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://192.168.2.173:80/WSclassif/ClassifierZip?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ClassifierZip_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ClassifierZip_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ClassifierZip_Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ClassifierZip_Service(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ClassifierZip_Service(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ClassifierZip_Service(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns ClassifierZip
     */
    @WebEndpoint(name = "ClassifierZipPort")
    public ClassifierZip getClassifierZipPort() {
        return super.getPort(ClassifierZipPort, ClassifierZip.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ClassifierZip
     */
    @WebEndpoint(name = "ClassifierZipPort")
    public ClassifierZip getClassifierZipPort(WebServiceFeature... features) {
        return super.getPort(ClassifierZipPort, ClassifierZip.class, features);
    }

}
