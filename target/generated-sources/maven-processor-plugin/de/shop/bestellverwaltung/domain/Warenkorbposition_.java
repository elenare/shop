package de.shop.bestellverwaltung.domain;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.339+0200")
@StaticMetamodel(Warenkorbposition.class)
public abstract class Warenkorbposition_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<Warenkorbposition, Artikel> artikel;
	public static volatile SingularAttribute<Warenkorbposition, Integer> anzahl;
	public static volatile SingularAttribute<Warenkorbposition, Long> id;
	public static volatile SingularAttribute<Warenkorbposition, AbstractKunde> kunde;

}

