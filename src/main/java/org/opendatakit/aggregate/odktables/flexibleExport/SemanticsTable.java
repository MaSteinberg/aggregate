package org.opendatakit.aggregate.odktables.flexibleExport;

import org.opendatakit.common.persistence.*;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class describes the Semantics table in the database
 * It stores meta-information about the fields of a survey, necessary for the template-based export
 *
 */
public class SemanticsTable extends CommonFieldsBase {
    private static final String TABLE_NAME = "_semantics";

    private static final DataField FORM_ID = new DataField("FORM_ID", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField FIELD_NAME = new DataField("FIELD_NAME", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField PROPERTY_NAME = new DataField("PROPERTY_NAME", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField PROPERTY_VALUE = new DataField("PROPERTY_VALUE", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static SemanticsTable relation = null;

    /**
     * Construct a relation prototype. Only called via {@link #assertRelation(CallingContext)}
     *
     * @param databaseSchema
     */
    private SemanticsTable(String databaseSchema){
        super(databaseSchema, TABLE_NAME);

        fieldList.add(FORM_ID);
        fieldList.add(FIELD_NAME);
        fieldList.add(PROPERTY_NAME);
        fieldList.add(PROPERTY_VALUE);
    }

    /**
     * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
     *
     * @param ref
     * @param user
     */
    private SemanticsTable(SemanticsTable ref, User user) {
        super(ref, user);
    }

    // Only called from within the persistence layer.
    @Override
    public SemanticsTable getEmptyRow(User user) {
        return new SemanticsTable(this, user);
    }

    // Getters and Setters
    public String getFormId(){
        return getStringField(FORM_ID);
    }

    public void setFormId(String value){
        if(!setStringField(FORM_ID, value)){
            throw new IllegalStateException("Overflow FormId");
        }
    }

    public String getFieldName(){
        return getStringField(FIELD_NAME);
    }

    public void setFieldName(String value){
        if(!setStringField(FIELD_NAME, value)){
            throw new IllegalStateException("Overflow FieldName");
        }
    }

    public String getPropertyName(){
        return getStringField(PROPERTY_NAME);
    }

    public void setPropertyName(String value){
        if(!setStringField(PROPERTY_NAME, value)){
            throw new IllegalStateException("Overflow PropertyName");
        }
    }

    public String getPropertyValue(){
        return getStringField(PROPERTY_VALUE);
    }

    public void setPropertyValue(String value){
        if(!setStringField(PROPERTY_VALUE, value)){
            throw new IllegalStateException("Overflow PropertyValue");
        }
    }

    // Creates the relation in the datastore if it doesn't exist yet and returns the table to work with
    private static synchronized final SemanticsTable assertRelation(CallingContext cc) throws ODKDatastoreException{
        if(relation == null){
            Datastore ds = cc.getDatastore();
            User user = cc.getCurrentUser();
            SemanticsTable relationPrototype;
            relationPrototype = new SemanticsTable(ds.getDefaultSchemaName());
            ds.assertRelation(relationPrototype, user);
            relation = relationPrototype;
        }
        return relation;
    }

    // Persists an entity with the given parameters in the Semantics-DB-Table
    public static final SemanticsTable assertSemantics(String formId, String fieldName, String propertyName, String propertyValue, CallingContext cc) throws ODKDatastoreException{
        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();

        SemanticsTable st;
        SemanticsTable stRelation = SemanticsTable.assertRelation(cc);
        st = ds.createEntityUsingRelation(stRelation, user);
        st.setFormId(formId);
        st.setFieldName(fieldName);
        st.setPropertyName(propertyName);
        st.setPropertyValue(propertyValue);
        ds.putEntity(st, user);

        return st;
    }

    /*
    Returns the stored semantics of the form with the given formId
     */
    public static final List<SemanticsTable> findEntriesByFormId(String formId, CallingContext cc){
        List<SemanticsTable> out = new ArrayList();
        try{
            SemanticsTable stRelation = SemanticsTable.assertRelation(cc);
            Query q = cc.getDatastore().createQuery(stRelation, "SemanticsTable.findEntriesByFormId", cc.getCurrentUser());
            q.addFilter(SemanticsTable.FORM_ID, Query.FilterOperation.EQUAL, formId);
            List<? extends CommonFieldsBase> l = q.executeQuery();
            for(CommonFieldsBase b : l){
                SemanticsTable t = (SemanticsTable) b;
                if( t.getFormId().equals(formId) ){
                    out.add(t);
                }
            }
        } catch(ODKDatastoreException e){
            e.printStackTrace();
        }
        return out;
    }

    /*
    Deletes an entry form the table
     */
    public void delete(CallingContext cc) throws ODKDatastoreException {
        Datastore ds = cc.getDatastore();
        ds.deleteEntity(this.getEntityKey(), cc.getCurrentUser());
    }
}
