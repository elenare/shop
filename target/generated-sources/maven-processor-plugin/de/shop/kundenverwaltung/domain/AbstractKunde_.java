package de.shop.kundenverwaltung.domain;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.persistence.File;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.287+0200")
@StaticMetamodel(AbstractKunde.class)
public abstract class AbstractKunde_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<AbstractKunde, BigDecimal> umsatz;
	public static volatile SingularAttribute<AbstractKunde, Boolean> newsletter;
	public static volatile SingularAttribute<AbstractKunde, String> bemerkungen;
	public static volatile SingularAttribute<AbstractKunde, BigDecimal> rabatt;
	public static volatile SingularAttribute<AbstractKunde, File> file;
	public static volatile ListAttribute<AbstractKunde, Reklamation> reklamationen;
	public static volatile SingularAttribute<AbstractKunde, String> loginname;
	public static volatile ListAttribute<AbstractKunde, Bestellung> bestellungen;
	public static volatile SingularAttribute<AbstractKunde, Integer> kategorie;
	public static volatile SingularAttribute<AbstractKunde, Date> seit;
	public static volatile SingularAttribute<AbstractKunde, Long> id;

}

