package de.shop.bestellverwaltung.domain;

import de.shop.artikelverwaltung.domain.Artikel;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.387+0200")
@StaticMetamodel(Bestellposition.class)
public abstract class Bestellposition_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<Bestellposition, Artikel> artikel;
	public static volatile SingularAttribute<Bestellposition, Integer> anzahl;
	public static volatile SingularAttribute<Bestellposition, Long> id;

}

