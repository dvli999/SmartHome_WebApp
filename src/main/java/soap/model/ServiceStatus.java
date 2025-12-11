package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "ServiceStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceStatus implements Serializable {

    @XmlElement(required = true)
    private String corba;

    @XmlElement(required = true)
    private String rmi;

    @XmlElement(required = true)
    private String ml;

    @XmlElement(required = true)
    private String nameService;

    @XmlElement(required = true)
    private String webServer;

    @XmlElement(required = true)
    private String soap;

    public ServiceStatus() {
    }

    public String getCorba() {
        return corba;
    }

    public void setCorba(String corba) {
        this.corba = corba;
    }

    public String getRmi() {
        return rmi;
    }

    public void setRmi(String rmi) {
        this.rmi = rmi;
    }

    public String getMl() {
        return ml;
    }

    public void setMl(String ml) {
        this.ml = ml;
    }

    public String getNameService() {
        return nameService;
    }

    public void setNameService(String nameService) {
        this.nameService = nameService;
    }

    public String getWebServer() {
        return webServer;
    }

    public void setWebServer(String webServer) {
        this.webServer = webServer;
    }

    public String getSoap() {
        return soap;
    }

    public void setSoap(String soap) {
        this.soap = soap;
    }
}
