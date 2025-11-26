package de.cramer.releasenotifier.providers.pdfmagazin

import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface PdfMagazinMagazineRepository :
    JpaRepository<PdfMagazinMagazine, Long>,
    JpaSpecificationExecutor<PdfMagazinMagazine>
