package org.opendatakit.aggregate.odktables.rdf;

import com.google.appengine.repackaged.com.google.datastore.v1.client.DatastoreException;
import org.opendatakit.common.persistence.*;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author markus.daniel.steinberg@uni-jena.de
 *
 */
public class SemanticsTable extends CommonFieldsBase {
    private static final String TABLE_NAME = "_semantics";

    private static final DataField URI_MD5_FORM_ID = new DataField("URI_MD5_FORM_ID", DataField.DataType.URI,
            false, PersistConsts.URI_STRING_LEN).setIndexable(DataField.IndexType.HASH);

    private static final DataField FORM_ID = new DataField("FORM_ID", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField FIELD_NAME = new DataField("FIELD_NAME", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField METRIC_NAME = new DataField("METRIC_NAME", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static final DataField METRIC_VALUE = new DataField("METRIC_VALUE", DataField.DataType.STRING,
            false, PersistConsts.DEFAULT_MAX_STRING_LENGTH);

    private static SemanticsTable relation = null;

    /**
     * Construct a relation prototype. Only called via {@link #assertRelation(CallingContext)}
     *
     * @param databaseSchema
     */
    private SemanticsTable(String databaseSchema){
        super(databaseSchema, TABLE_NAME);

        fieldList.add(URI_MD5_FORM_ID);
        fieldList.add(FORM_ID);
        fieldList.add(FIELD_NAME);
        fieldList.add(METRIC_NAME);
        fieldList.add(METRIC_VALUE);
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

    public String getUriMd5FormId(){
        return getStringField(URI_MD5_FORM_ID);
    }

    public void setUriMd5FormId(String value){
        if(!setStringField(URI_MD5_FORM_ID, value)){
            throw new IllegalStateException("Overflow UriMd5FormId");
        }
    }

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

    public String getMetricName(){
        return getStringField(METRIC_NAME);
    }

    public void setMetricName(String value){
        if(!setStringField(METRIC_NAME, value)){
            throw new IllegalStateException("Overflow MetricName");
        }
    }

    public String getMetricValue(){
        return getStringField(METRIC_VALUE);
    }

    public void setMetricValue(String value){
        if(!setStringField(METRIC_VALUE, value)){
            throw new IllegalStateException("Overflow MetricValue");
        }
    }

    private static synchronized final SemanticsTable assertRelation(CallingContext cc) throws ODKDatastoreException{
        if(relation == null){
            Datastore ds = cc.getDatastore();
            User user = cc.getCurrentUser();
            SemanticsTable relationPrototype;
            relationPrototype = new SemanticsTable(ds.getDefaultSchemaName());
            ds.assertRelation(relationPrototype, user); // may throw exception...
            // at this point, the prototype has become fully populated
            relation = relationPrototype; // set static variable only upon success...
        }
        return relation;
    }

    public static final SemanticsTable assertSemantics(String formId, String fieldName, String metricName, String metricValue, CallingContext cc) throws ODKDatastoreException{
        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();

        SemanticsTable st;
        SemanticsTable stRelation = SemanticsTable.assertRelation(cc);
        st = ds.createEntityUsingRelation(stRelation, user);
        st.setUriMd5FormId(CommonFieldsBase.newMD5HashUri(formId));
        st.setFormId(formId);
        st.setFieldName(fieldName);
        st.setMetricName(metricName);
        st.setMetricValue(metricValue);
        ds.putEntity(st, user);

        return st;
    }

    public static final List<SemanticsTable> findEntriesByFormId(String formId, CallingContext cc){
        List out = new ArrayList();
        try{
            SemanticsTable stRelation = SemanticsTable.assertRelation(cc);
            String formIdMd5 = CommonFieldsBase.newMD5HashUri(formId);
            Query q = cc.getDatastore().createQuery(stRelation, "SemanticsTable.findEntriesByFormId", cc.getCurrentUser());
            q.addFilter(SemanticsTable.URI_MD5_FORM_ID, Query.FilterOperation.EQUAL, formIdMd5);
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
}
