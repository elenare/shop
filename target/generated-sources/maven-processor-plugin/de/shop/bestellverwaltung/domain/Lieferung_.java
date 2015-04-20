package de.shop.bestellverwaltung.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.394+0200")
@StaticMetamodel(Lieferung.class)
public abstract class Lieferung_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<Lieferung, TransportType> transportArt;
	public static volatile SetAttribute<Lieferung, Bestellung> bestellungen;
	public static volatile SingularAttribute<Lieferung, Long> id;
	public static volatile SingularAttribute<Lieferung, String> liefernr;

}

