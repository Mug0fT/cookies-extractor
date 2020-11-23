package com.mugoft.storageproviders;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mugoft.storageproviders.common.StorageProvider;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;


/**
 * @author mugoft
 * @created 22/11/2020 - 19:36
 * @project cookiesextractor
 */
public class S3BucketStorageProvider extends StorageProvider {
    String bucketName = "realestate-parser-bucket";
    String stringObjKeyName = "*** String object key name ***";
    String fileObjKeyName = "cookies/wikipedia_mainpage_singlerequest_s3";
//    String fileName = "cookies/wikipedia_selenium/cookies.json";

    public S3BucketStorageProvider(String outPath) {
        super(outPath);
    }

    public void storeCookies(String jsonCookies) throws Exception {

        try {
//           InstanceProfileCredentialsProvider credentials = InstanceProfileCredentialsProvider.builder().asyncCredentialUpdateEnabled(true).build();

            AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
            System.out.println(s3client.listBuckets());
            if (!(s3client.doesBucketExistV2(bucketName))) {
                throw new AmazonS3Exception("S3 bucket doesn't exist or you don't have access to it: " + outPath);
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("title", "someTitle");
            PutObjectRequest request = new PutObjectRequest(bucketName, outPath, IOUtils.toInputStream(jsonCookies, StandardCharsets.UTF_8.name()), metadata);
            s3client.putObject(request);
        } catch (AmazonServiceException ex) {
            System.out.println("Error during uploading cookies to s3 bucket");
            throw ex;
        } catch (SdkClientException e) {
            System.out.println("Amazon S3 couldn't be contacted for a response, or the client couldn't parse the response from Amazon S3.");
            throw e;
        }
    }
}
