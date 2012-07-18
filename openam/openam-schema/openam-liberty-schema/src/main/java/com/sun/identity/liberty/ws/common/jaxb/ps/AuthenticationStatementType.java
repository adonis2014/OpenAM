//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.6-b27-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.11 at 10:33:54 AM PDT 
//


package com.sun.identity.liberty.ws.common.jaxb.ps;


/**
 * Java content class for AuthenticationStatementType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/allan/A-SVN/trunk/opensso/products/federation/library/xsd/liberty/lib-arch-protocols-schema.xsd line 176)
 * <p>
 * <pre>
 * &lt;complexType name="AuthenticationStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}AuthenticationStatementType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:liberty:iff:2003-08}AuthnContext" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ReauthenticateOnOrAfter" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="SessionIndex" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface AuthenticationStatementType
    extends com.sun.identity.liberty.ws.common.jaxb.assertion.AuthenticationStatementType
{


    /**
     * Gets the value of the authnContext property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextElement}
     *     {@link com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextType}
     */
    com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextType getAuthnContext();

    /**
     * Sets the value of the authnContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextElement}
     *     {@link com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextType}
     */
    void setAuthnContext(com.sun.identity.liberty.ws.common.jaxb.ps.AuthnContextType value);

    /**
     * Gets the value of the reauthenticateOnOrAfter property.
     * 
     * @return
     *     possible object is
     *     {@link java.util.Calendar}
     */
    java.util.Calendar getReauthenticateOnOrAfter();

    /**
     * Sets the value of the reauthenticateOnOrAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.util.Calendar}
     */
    void setReauthenticateOnOrAfter(java.util.Calendar value);

    /**
     * Gets the value of the sessionIndex property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getSessionIndex();

    /**
     * Sets the value of the sessionIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setSessionIndex(java.lang.String value);

}
