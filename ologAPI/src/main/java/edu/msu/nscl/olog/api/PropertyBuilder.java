/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.msu.nscl.olog.api;

/**
 *
 * @author berryman
 */
public class PropertyBuilder {

		//required
		private String name;
                private String value;

		/**
		 * @param name
                 *
		 */
		public static PropertyBuilder property(String name) {
			PropertyBuilder propertyBuilder = new PropertyBuilder();
			propertyBuilder.name = name;
			return propertyBuilder;
		}

                /**
		 * @param name
                 * @param value
		 */
		public static PropertyBuilder property(String name,String value) {
			PropertyBuilder propertyBuilder = new PropertyBuilder();
			propertyBuilder.name = name;
                        propertyBuilder.value = value;
			return propertyBuilder;
		}

		public static PropertyBuilder property(Property property) {
			PropertyBuilder propertyBuilder = new PropertyBuilder();
			propertyBuilder.name = property.getName();
                        propertyBuilder.value = property.getValue();
			return propertyBuilder;
		}

                public PropertyBuilder value(String value){
                        this.value = value;
                        return this;
                }

		XmlProperty toXml() {
			return new XmlProperty(name,value);
		}

		Property build() {
			return new Property(this.toXml());
		}

}
