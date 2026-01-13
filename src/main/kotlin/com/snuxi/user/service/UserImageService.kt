package com.snuxi.user.service

import com.snuxi.user.EmptyFileException
import com.snuxi.user.InvalidImageFormatException
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.repository.UserRepository
import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class UserImageService(
    private val userRepository: UserRepository,
    private val s3Template: S3Template,
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucketName: String
) {
    @Transactional
    fun uploadProfileImage(email: String, file: MultipartFile): String {
        if (file.isEmpty) {
            throw EmptyFileException()
        }

        if (file.contentType?.startsWith("image") != true) {
            throw InvalidImageFormatException()
        }

        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        val oldImageUrl = user.profileImageUrl
        val originalName = file.originalFilename
        val extension = originalName?.substringAfterLast(".")
        val savedFileName = "${UUID.randomUUID()}.$extension"

        val metadata = ObjectMetadata.builder()
            .contentType(file.contentType)
            .build()

        val resource = s3Template.upload(
            bucketName,
            savedFileName,
            file.inputStream,
            metadata
        )

        val newImageUrl = resource.url.toString()

        user.profileImageUrl = newImageUrl
        if(!oldImageUrl.isNullOrBlank()) {
            deleteProfileImage(oldImageUrl)
        }

        return newImageUrl
    }

    private fun deleteProfileImage(url: String) {
        try {
            val path = java.net.URI(url).path
            val filename = path.substringAfterLast("/")
            s3Template.deleteObject(bucketName, filename)
        } catch (e: Exception) { }
    }
}