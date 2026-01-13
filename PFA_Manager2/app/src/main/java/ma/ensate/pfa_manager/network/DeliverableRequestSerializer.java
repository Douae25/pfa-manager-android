package ma.ensate.pfa_manager.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import ma.ensate.pfa_manager.model.api.DeliverableRequest;

public class DeliverableRequestSerializer implements JsonSerializer<DeliverableRequest> {
    
    @Override
    public JsonElement serialize(DeliverableRequest src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        
        // OBLIGATOIRE: pfaId
        if (src.getPfaId() != null) {
            json.addProperty("pfaId", src.getPfaId());
        }
        
        // OBLIGATOIRE: fileTitle
        if (src.getFileTitle() != null) {
            json.addProperty("fileTitle", src.getFileTitle());
        }
        
        // OBLIGATOIRE: fileUri (backend expects fileUri, not filePath)
        if (src.getFilePath() != null) {
            json.addProperty("fileUri", src.getFilePath());
        }
        
        // OBLIGATOIRE: deliverableType = BEFORE_DEFENSE or AFTER_DEFENSE
        if (src.getDeliverableType() != null) {
            json.addProperty("deliverableType", src.getDeliverableType().name());
        }
        
        // OBLIGATOIRE: fileType = PROGRESS_REPORT, PRESENTATION, FINAL_REPORT
        // Map client's DeliverableFileType using getBackendFileType()
        if (src.getFileType() != null) {
            json.addProperty("fileType", src.getBackendFileType());
        }
        
        return json;
    }
}
