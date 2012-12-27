//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.20 at 05:04:40 PM JST 
//


package gen.core.tbx;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}transac"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{}transacNote"/>
 *           &lt;element ref="{}date"/>
 *           &lt;element ref="{}note"/>
 *           &lt;element ref="{}ref"/>
 *           &lt;element ref="{}xref"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "transac",
    "transacNoteOrDateOrNote"
})
@XmlRootElement(name = "transacGrp")
public class TransacGrp {

    @XmlElement(required = true)
    protected Transac transac;
    @XmlElements({
        @XmlElement(name = "transacNote", type = TransacNote.class),
        @XmlElement(name = "xref", type = Xref.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "date", type = Date.class),
        @XmlElement(name = "ref", type = Ref.class)
    })
    protected List<Object> transacNoteOrDateOrNote;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the transac property.
     * 
     * @return
     *     possible object is
     *     {@link Transac }
     *     
     */
    public Transac getTransac() {
        return transac;
    }

    /**
     * Sets the value of the transac property.
     * 
     * @param value
     *     allowed object is
     *     {@link Transac }
     *     
     */
    public void setTransac(Transac value) {
        this.transac = value;
    }

    /**
     * Gets the value of the transacNoteOrDateOrNote property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transacNoteOrDateOrNote property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransacNoteOrDateOrNote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransacNote }
     * {@link Xref }
     * {@link Note }
     * {@link Date }
     * {@link Ref }
     * 
     * 
     */
    public List<Object> getTransacNoteOrDateOrNote() {
        if (transacNoteOrDateOrNote == null) {
            transacNoteOrDateOrNote = new ArrayList<Object>();
        }
        return this.transacNoteOrDateOrNote;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
