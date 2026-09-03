package com.creditscoring.service;

import com.creditscoring.entity.Banque;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;

@Service
public class ContratPdfServiceImpl
        implements ContratPdfService {

    @Override
    public byte[] genererContratPdf(Contrat contrat) {

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            com.lowagie.text.PageSize.A4,
                            36,
                            36,
                            36,
                            36
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // =====================================================
            // FONTS
            // =====================================================

            Font titre =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            20
                    );

            Font sousTitre =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12
                    );

            Font texte =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            10
                    );

            Font texteGras =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10
                    );

            Font petit =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            8
                    );

            // =====================================================
            // DONNÉES
            // =====================================================

            DemandeCredit demande =
                    contrat.getDemandeCredit();

            Client client =
                    demande != null
                            ? demande.getClient()
                            : null;

            Banque banque =
                    demande != null
                            ? demande.getBanque()
                            : null;

            // =====================================================
            // INFORMATIONS CLIENT
            // =====================================================

            String nomClient =
                    client != null
                            ? valeurTexte(
                            client.getPrenom()
                                    + " "
                                    + client.getNom()
                    )
                            : "";

            String dateNaissance =
                    client != null
                            && client.getDateNaissance() != null
                            ? client.getDateNaissance().toString()
                            : "";

            String cin =
                    client != null
                            ? valeurTexte(client.getCin())
                            : "";

            String adresse =
                    client != null
                            ? valeurTexte(client.getAdresse())
                            : "";

            String telephone =
                    client != null
                            ? valeurTexte(client.getTelephone())
                            : "";

            String email =
                    client != null
                            ? valeurTexte(client.getEmail())
                            : "";

            String profession =
                    client != null
                            ? valeurTexte(client.getProfession())
                            : "";

            String revenu =
                    client != null
                            && client.getRevenuMensuel() != null
                            ? formatDouble(
                            client.getRevenuMensuel()
                    ) + " TND"
                            : "";

            // =====================================================
            // CONTACT SOCIÉTÉ
            // =====================================================

            String adresseSociete = "";
            String telephoneSociete = "";
            String emailSociete = "";
            String siteSociete = "";

            if (banque != null) {

                adresseSociete =
                        valeurTexte(
                                banque.getAdresse()
                        );

                telephoneSociete =
                        valeurTexte(
                                banque.getTelephone()
                        );

                emailSociete =
                        valeurTexte(
                                banque.getEmail()
                        );

                siteSociete =
                        valeurTexte(
                                banque.getSiteWeb()
                        );
            }

            // =====================================================
            // HEADER
            // =====================================================

            PdfPTable header =
                    new PdfPTable(2);

            header.setWidthPercentage(100);

            header.setWidths(
                    new float[]{1.3f, 1f}
            );

            // -----------------------------------------------------
            // CREDITNOVA
            // -----------------------------------------------------

            PdfPCell societeCell =
                    new PdfPCell();

            societeCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            Paragraph societe =
                    new Paragraph(
                            "CREDITNOVA",
                            titre
                    );

            societe.setAlignment(
                    Element.ALIGN_LEFT
            );

            societeCell.addElement(
                    societe
            );

            Paragraph slogan =
                    new Paragraph(
                            "Smart Credit, Better Future",
                            petit
                    );

            societeCell.addElement(
                    slogan
            );

            header.addCell(
                    societeCell
            );

            // -----------------------------------------------------
            // TITRE CONTRAT
            // -----------------------------------------------------

            PdfPCell titreCell =
                    new PdfPCell();

            titreCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            titreCell.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            Paragraph titreContrat =
                    new Paragraph(
                            "CONTRAT DE CRÉDIT",
                            titre
                    );

            titreContrat.setAlignment(
                    Element.ALIGN_RIGHT
            );

            titreCell.addElement(
                    titreContrat
            );

            Paragraph numero =
                    new Paragraph(
                            "N° "
                                    + valeurTexte(
                                    contrat.getNumeroContrat()
                            ),
                            texteGras
                    );

            numero.setAlignment(
                    Element.ALIGN_RIGHT
            );

            titreCell.addElement(
                    numero
            );

            header.addCell(
                    titreCell
            );

            document.add(header);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // INTRODUCTION
            // =====================================================

            document.add(
                    new Paragraph(
                            "Le présent contrat est établi entre "
                                    + "CREDITNOVA et le client désigné "
                                    + "ci-dessous dans le cadre de "
                                    + "l'octroi du crédit.",
                            texte
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // 1. INFORMATIONS CLIENT
            // =====================================================

            document.add(
                    new Paragraph(
                            "1. INFORMATIONS DU CLIENT",
                            sousTitre
                    )
            );

            PdfPTable clientTable =
                    new PdfPTable(2);

            clientTable.setWidthPercentage(100);

            ajouterLigne(
                    clientTable,
                    "Nom complet",
                    nomClient,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Date de naissance",
                    dateNaissance,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "CIN / Passeport",
                    cin,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Adresse",
                    adresse,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Téléphone",
                    telephone,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Email",
                    email,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Situation professionnelle",
                    profession,
                    texteGras,
                    texte
            );

            ajouterLigne(
                    clientTable,
                    "Revenu mensuel",
                    revenu,
                    texteGras,
                    texte
            );

            document.add(
                    clientTable
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // 2. DÉTAILS CRÉDIT
            // =====================================================

            document.add(
                    new Paragraph(
                            "2. DÉTAILS DU CRÉDIT",
                            sousTitre
                    )
            );

            PdfPTable creditTable =
                    new PdfPTable(2);

            creditTable.setWidthPercentage(100);

            ajouterLigne(
                    creditTable,
                    "Montant",
                    contrat.getMontant() != null
                            ? formatDouble(
                            contrat.getMontant()
                    ) + " TND"
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Durée",
                    demande != null
                            && demande.getDuree() != null
                            ? demande.getDuree() + " mois"
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Taux d'intérêt",
                    contrat.getTauxInteret() != null
                            ? formatDouble(
                            contrat.getTauxInteret()
                    ) + " %"
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Mensualité",
                    contrat.getMensualite() != null
                            ? formatDouble(
                            contrat.getMensualite()
                    ) + " TND"
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Coût total",
                    contrat.getCoutTotal() != null
                            ? formatDouble(
                            contrat.getCoutTotal()
                    ) + " TND"
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Date de début",
                    contrat.getDateDebut() != null
                            ? contrat.getDateDebut().toString()
                            : "",
                    texteGras,
                    texte
            );

            ajouterLigne(
                    creditTable,
                    "Date de fin",
                    contrat.getDateFin() != null
                            ? contrat.getDateFin().toString()
                            : "",
                    texteGras,
                    texte
            );

            document.add(
                    creditTable
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // 3. SIGNATURES
            // =====================================================

            document.add(
                    new Paragraph(
                            "3. SIGNATURES",
                            sousTitre
                    )
            );

            PdfPTable signatures =
                    new PdfPTable(2);

            signatures.setWidthPercentage(100);

            // =====================================================
            // SIGNATURE CREDITNOVA
            // =====================================================

            PdfPCell societeSignatureCell =
                    new PdfPCell();

            societeSignatureCell.setPadding(10);

            societeSignatureCell.addElement(
                    new Paragraph(
                            "Pour CREDITNOVA",
                            texteGras
                    )
            );

            societeSignatureCell.addElement(
                    new Paragraph(" ")
            );

            Paragraph titreSignature =
                    new Paragraph(
                            "Signature officielle",
                            texteGras
                    );

            titreSignature.setAlignment(
                    Element.ALIGN_CENTER
            );

            societeSignatureCell.addElement(
                    titreSignature
            );

            societeSignatureCell.addElement(
                    new Paragraph(" ")
            );

            // -----------------------------------------------------
            // TABLE SIGNATURE + CACHET
            // -----------------------------------------------------

            PdfPTable signatureCachet =
                    new PdfPTable(2);

            signatureCachet.setWidthPercentage(100);

            signatureCachet.setWidths(
                    new float[]{1.2f, 1f}
            );

            // =====================================================
            // SIGNATURE FIXE CREDITNOVA
            // =====================================================

            PdfPCell signatureCell =
                    new PdfPCell();

            signatureCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            signatureCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            Image signatureImage =
                    chargerImage(
                            "signatures/signature-creditnova.png",
                            210,
                            90
                    );

            if (signatureImage != null) {

                signatureCell.addElement(
                        signatureImage
                );

            } else {

                Paragraph p =
                        new Paragraph(
                                "Signature CREDITNOVA "
                                        + "non disponible",
                                petit
                        );

                p.setAlignment(
                        Element.ALIGN_CENTER
                );

                signatureCell.addElement(
                        p
                );
            }

            signatureCell.addElement(
                    new Paragraph(" ")
            );

            Paragraph signatureLabel =
                    new Paragraph(
                            "SIGNATURE OFFICIELLE DE CREDITNOVA",
                            petit
                    );

            signatureLabel.setAlignment(
                    Element.ALIGN_CENTER
            );

            signatureCell.addElement(
                    signatureLabel
            );

            signatureCachet.addCell(
                    signatureCell
            );

            // =====================================================
            // CACHET CREDITNOVA FIXE
            // =====================================================

            PdfPCell cachetCell =
                    new PdfPCell();

            cachetCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            cachetCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            Image cachetImage =
                    chargerImage(
                            "signatures/creditnova-cachet.png",
                            100,
                            100
                    );

            if (cachetImage != null) {

                cachetCell.addElement(
                        cachetImage
                );

            } else {

                Paragraph p =
                        new Paragraph(
                                "Cachet non disponible",
                                petit
                        );

                p.setAlignment(
                        Element.ALIGN_CENTER
                );

                cachetCell.addElement(
                        p
                );
            }

            signatureCachet.addCell(
                    cachetCell
            );

            societeSignatureCell.addElement(
                    signatureCachet
            );

            societeSignatureCell.addElement(
                    new Paragraph(" ")
            );

            societeSignatureCell.addElement(
                    new Paragraph(
                            "Date : "
                                    + java.time.LocalDate.now(),
                            texte
                    )
            );

            signatures.addCell(
                    societeSignatureCell
            );

            // =====================================================
            // CLIENT
            // =====================================================

            PdfPCell clientSignatureCell =
                    new PdfPCell();

            clientSignatureCell.setPadding(10);

            clientSignatureCell.addElement(
                    new Paragraph(
                            "Le Client",
                            texteGras
                    )
            );

            clientSignatureCell.addElement(
                    new Paragraph(
                            "Nom et Prénom : "
                                    + nomClient,
                            texte
                    )
            );

            clientSignatureCell.addElement(
                    new Paragraph(
                            "CIN / Passeport : "
                                    + cin,
                            texte
                    )
            );

            clientSignatureCell.addElement(
                    new Paragraph(" ")
            );

            // =====================================================
            // SIGNATURE CLIENT
            // =====================================================

            if (contrat.getSignatureClient() != null
                    && !contrat.getSignatureClient().isBlank()) {

                clientSignatureCell.addElement(
                        new Paragraph(
                                "Signature électronique :",
                                texteGras
                        )
                );

                clientSignatureCell.addElement(
                        new Paragraph(
                                contrat.getSignatureClient(),
                                texte
                        )
                );

                String dateSignature = contrat.getDateSignature() != null
                        ? contrat.getDateSignature()
                        .format(
                                java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy HH:mm")
                        )
                        : "";

                clientSignatureCell.addElement(
                        new Paragraph(
                                "Date de signature : " + dateSignature,
                                texte
                        )
                );

            } else {

                clientSignatureCell.addElement(
                        new Paragraph(
                                "Signature :",
                                texte
                        )
                );

                clientSignatureCell.addElement(
                        new Paragraph(
                                "\n\n________________________",
                                texte
                        )
                );

                clientSignatureCell.addElement(
                        new Paragraph(
                                "Date : __________________",
                                texte
                        )
                );
            }

            signatures.addCell(
                    clientSignatureCell
            );

            document.add(
                    signatures
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // 4. DOCUMENTS REMIS
            // =====================================================

            document.add(
                    new Paragraph(
                            "4. DOCUMENTS REMIS AU CLIENT",
                            sousTitre
                    )
            );

            document.add(
                    new Paragraph(
                            "• Une copie du présent contrat de crédit",
                            texte
                    )
            );

            document.add(
                    new Paragraph(
                            "• Les documents contractuels applicables",
                            texte
                    )
            );

            document.add(
                    new Paragraph(
                            "• Le récapitulatif du crédit",
                            texte
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // 5. CONTACT
            // =====================================================

            document.add(
                    new Paragraph(
                            "5. CONTACT",
                            sousTitre
                    )
            );

            ajouterContact(
                    document,
                    "Adresse",
                    adresseSociete,
                    texte
            );

            ajouterContact(
                    document,
                    "Téléphone",
                    telephoneSociete,
                    texte
            );

            ajouterContact(
                    document,
                    "Site web",
                    siteSociete,
                    texte
            );

            ajouterContact(
                    document,
                    "Email",
                    emailSociete,
                    texte
            );

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // FOOTER
            // =====================================================

            Paragraph footer =
                    new Paragraph(
                            "CREDITNOVA - Document généré automatiquement.",
                            petit
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(
                    footer
            );

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur lors de la génération du PDF : "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // HELPER : LIGNE TABLE
    // =========================================================

    private void ajouterLigne(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                value != null
                                        ? value
                                        : "",
                                valueFont
                        )
                );

        labelCell.setPadding(6);
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // =========================================================
    // HELPER : CONTACT
    // =========================================================

    private void ajouterContact(
            Document document,
            String label,
            String value,
            Font font
    ) {

        document.add(
                new Paragraph(
                        label
                                + " : "
                                + valeurTexte(value),
                        font
                )
        );
    }

    // =========================================================
    // HELPER : CHARGER IMAGE
    // =========================================================

    private Image chargerImage(
            String resourcePath,
            float largeur,
            float hauteur
    ) {

        try {

            URL resource =
                    getClass()
                            .getClassLoader()
                            .getResource(resourcePath);

            if (resource == null) {
                return null;
            }

            Image image =
                    Image.getInstance(resource);

            image.scaleToFit(
                    largeur,
                    hauteur
            );

            image.setAlignment(
                    Element.ALIGN_CENTER
            );

            return image;

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // HELPER : TEXTE
    // =========================================================

    private String valeurTexte(
            String value
    ) {

        return value != null
                ? value
                : "";
    }

    // =========================================================
    // HELPER : DOUBLE
    // =========================================================

    private String formatDouble(
            Double value
    ) {

        return value != null
                ? String.format(
                "%.2f",
                value
        )
                : "";
    }
}