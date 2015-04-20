package de.shop.util.persistence;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor", date = "2015-04-14T22:06:55.325+0200")
@StaticMetamodel(File.class)
public abstract class File_ extends de.shop.util.persistence.AbstractVersionedAuditable_ {

	public static volatile SingularAttribute<File, MultimediaType> multimediaType;
	public static volatile SingularAttribute<File, String> filename;
	public static volatile SingularAttribute<File, byte[]> bytes;
	public static volatile SingularAttribute<File, Long> id;
	public static volatile SingularAttribute<File, MimeType> mimeType;

}

