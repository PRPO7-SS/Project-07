package ss.finance.serializer;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ObjectIdSerializer extends JsonSerializer<ObjectId>{

    @Override
    public void serialize(ObjectId objectId, JsonGenerator jg, SerializerProvider sp) throws IOException {
        jg.writeString(objectId.toHexString());
    }
    
}
