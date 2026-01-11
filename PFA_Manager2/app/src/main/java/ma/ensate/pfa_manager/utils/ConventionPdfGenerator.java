package ma.ensate.pfa_manager.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import android.graphics.pdf.PdfDocument;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ma.ensate.pfa_manager.model.Convention;

public class ConventionPdfGenerator {

    public static void generateAndDownloadPdf(Context context, Convention convention) {
        try {
            String fileName = "Convention_" + convention.getPfa_id() + "_" + System.currentTimeMillis() + ".pdf";

            // Stocker dans le dossier d'app (pas de permission requise)
            File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir != null && !downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File pdfFile = new File(downloadsDir, fileName);
            createPdf(convention, pdfFile);

            Toast.makeText(context, "Convention générée: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Ouvrir automatiquement le fichier généré
            openFile(context, pdfFile);
        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static void createPdf(Convention convention, File pdfFile) throws Exception {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 at 72dpi
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        float centerX = pageInfo.getPageWidth() / 2f;
        int y = 60;
        int line = 26;

        Paint titlePaint = new Paint();
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(18f);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        Paint sectionPaint = new Paint();
        sectionPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        sectionPaint.setTextSize(14f);

        Paint labelPaint = new Paint();
        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        labelPaint.setTextSize(12f);

        Paint valuePaint = new Paint();
        valuePaint.setTextSize(12f);

        // Title
        canvas.drawText("CONVENTION DE STAGE", centerX, y, titlePaint);
        y += 30;

        // Dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = dateFormat.format(new Date(convention.getStart_date()));
        String endDate = dateFormat.format(new Date(convention.getEnd_date()));

        // Entreprise
        y += 20;
        canvas.drawText("Informations de l'entreprise", 40, y, sectionPaint);
        y += line;
        drawLine(canvas, labelPaint, valuePaint, "Nom:", safe(convention.getCompany_name()), y); y += line;
        drawLine(canvas, labelPaint, valuePaint, "Adresse:", safe(convention.getCompany_address()), y); y += line;

        // Superviseur
        y += 16;
        canvas.drawText("Superviseur", 40, y, sectionPaint);
        y += line;
        drawLine(canvas, labelPaint, valuePaint, "Nom:", safe(convention.getCompany_supervisor_name()), y); y += line;
        drawLine(canvas, labelPaint, valuePaint, "Email:", safe(convention.getCompany_supervisor_email()), y); y += line;

        // Dates de stage
        y += 16;
        canvas.drawText("Dates de stage", 40, y, sectionPaint);
        y += line;
        drawLine(canvas, labelPaint, valuePaint, "Du:", startDate, y); y += line;
        drawLine(canvas, labelPaint, valuePaint, "Au:", endDate, y); y += line;

        // Footer
        y += 30;
        canvas.drawText("Cette convention a été générée automatiquement par le système PFA.", 40, y, valuePaint);
        y += line;
        canvas.drawText("Signez puis déposez la version scannée dans votre espace étudiant.", 40, y, valuePaint);

        document.finishPage(page);

        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        fos.close();
        document.close();
    }

    private static void drawLine(Canvas canvas, Paint label, Paint value, String l, String v, int y) {
        canvas.drawText(l, 40, y, label);
        canvas.drawText(v, 150, y, value);
    }

    private static String safe(String s) {
        return s == null ? "N/A" : s;
    }

    private static void openFile(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(context, "ma.ensate.pfa_manager.fileprovider", file);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime != null ? mime : "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Impossible d'ouvrir le PDF", Toast.LENGTH_SHORT).show();
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
