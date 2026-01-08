package ma.ensate.pfa_manager.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ma.ensate.pfa_manager.model.Convention;

public class ConventionPdfGenerator {

    public static void generateAndDownloadPdf(Context context, Convention convention) {
        try {
            String fileName = "Convention_" + convention.getPfa_id() + "_" + System.currentTimeMillis() + ".html";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            File htmlFile = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(htmlFile);
            writer.write(generateHtmlContent(convention));
            writer.close();
            
            Toast.makeText(context, "Convention générée: " + htmlFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static String generateHtmlContent(Convention convention) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = dateFormat.format(new Date(convention.getStart_date()));
        String endDate = dateFormat.format(new Date(convention.getEnd_date()));
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }" +
                "h1 { text-align: center; color: #0D47A1; }" +
                "h2 { color: #1976D2; border-bottom: 2px solid #1976D2; padding-bottom: 10px; margin-top: 30px; }" +
                ".section { margin: 20px 0; }" +
                ".label { font-weight: bold; color: #333; }" +
                ".value { color: #666; margin-left: 10px; }" +
                ".footer { margin-top: 50px; border-top: 1px solid #ddd; padding-top: 20px; font-size: 12px; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>CONVENTION DE STAGE</h1>" +
                
                "<div class='section'>" +
                "<h2>Informations de l'Entreprise</h2>" +
                "<p><span class='label'>Nom:</span><span class='value'>" + (convention.getCompany_name() != null ? convention.getCompany_name() : "N/A") + "</span></p>" +
                "<p><span class='label'>Adresse:</span><span class='value'>" + (convention.getCompany_address() != null ? convention.getCompany_address() : "N/A") + "</span></p>" +
                "</div>" +
                
                "<div class='section'>" +
                "<h2>Superviseur</h2>" +
                "<p><span class='label'>Nom:</span><span class='value'>" + (convention.getCompany_supervisor_name() != null ? convention.getCompany_supervisor_name() : "N/A") + "</span></p>" +
                "<p><span class='label'>Email:</span><span class='value'>" + (convention.getCompany_supervisor_email() != null ? convention.getCompany_supervisor_email() : "N/A") + "</span></p>" +
                "</div>" +
                
                "<div class='section'>" +
                "<h2>Dates de Stage</h2>" +
                "<p><span class='label'>Du:</span><span class='value'>" + startDate + "</span></p>" +
                "<p><span class='label'>Au:</span><span class='value'>" + endDate + "</span></p>" +
                "</div>" +
                
                "<div class='footer'>" +
                "<p>Cette convention a été générée automatiquement par le système de gestion PFA.</p>" +
                "<p>Scannez et signez cette convention, puis déposez-la dans votre espace étudiant.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
