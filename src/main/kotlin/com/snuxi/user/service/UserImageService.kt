package com.snuxi.user.service

import com.snuxi.user.EmptyFileException
import com.snuxi.user.InvalidImageFormatException
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Paths
import java.util.UUID

@Service
class UserImageService(
    private val userRepository: UserRepository
) {
    private val uploadDir =
        Paths.get("src/main/resources/static/images").toAbsolutePath().toString()

    @Transactional
    fun uploadProfileImage(email: String, file: MultipartFile): String {
        if (file.isEmpty) {
            throw EmptyFileException()
        }

        if (file.contentType == null || !file.contentType!!.startsWith("image")) {
            throw InvalidImageFormatException()
        }
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()

        val directory = File(uploadDir)
        if(!directory.exists()) {
            directory.mkdirs()
        }

        val originalName = file.originalFilename
        val extension = originalName?.substringAfterLast(".")
        val savedFileName = "${UUID.randomUUID()}.$extension"

        val savePath = Paths.get(uploadDir, savedFileName)
        file.transferTo(savePath.toFile())

        val accessUrl = "/images/$savedFileName"
        user.profileImageUrl = accessUrl

        return accessUrl
    }
}