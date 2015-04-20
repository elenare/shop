package de.shop.kundenverwaltung.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.264+0200")
@StaticMetamodel(Privatkunde.class)
public abstract class Privatkunde_ extends de.shop.kundenverwaltung.domain.AbstractKunde_ {

	public static volatile SetAttribute<Privatkunde, HobbyType> hobbys;
	public static volatile SingularAttribute<Privatkunde, GeschlechtType> geschlecht;
	public static volatile SingularAttribute<Privatkunde, FamilienstandType> familienstand;

}

