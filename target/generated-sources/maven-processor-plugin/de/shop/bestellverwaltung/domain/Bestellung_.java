package de.shop.bestellverwaltung.domain;

import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.400+0200")
@StaticMetamodel(Bestellung.class)
public abstract class Bestellung_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile ListAttribute<Bestellung, Bestellposition> bestellpositionen;
	public static volatile SetAttribute<Bestellung, Lieferung> lieferungen;
	public static volatile SingularAttribute<Bestellung, BigDecimal> gesamtbetrag;
	public static volatile SingularAttribute<Bestellung, Long> id;
	public static volatile SingularAttribute<Bestellung, AbstractKunde> kunde;

}

