package de.shop.kundenverwaltung.domain;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.311+0200")
@StaticMetamodel(Reklamation.class)
public abstract class Reklamation_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<Reklamation, Date> datum;
	public static volatile SingularAttribute<Reklamation, Long> nr;
	public static volatile SingularAttribute<Reklamation, String> inhalt;
	public static volatile SingularAttribute<Reklamation, AbstractKunde> kunde;

}

