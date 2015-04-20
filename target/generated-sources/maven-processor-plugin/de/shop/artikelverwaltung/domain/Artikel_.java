package de.shop.artikelverwaltung.domain;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.332+0200")
@StaticMetamodel(Artikel.class)
public abstract class Artikel_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<Artikel, BigDecimal> preis;
	public static volatile SingularAttribute<Artikel, String> bezeichnung;
	public static volatile SingularAttribute<Artikel, Integer> rating;
	public static volatile SingularAttribute<Artikel, Long> id;
	public static volatile SingularAttribute<Artikel, Boolean> ausgesondert;

}

