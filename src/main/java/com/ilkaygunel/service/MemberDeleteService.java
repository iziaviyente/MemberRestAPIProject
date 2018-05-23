package com.ilkaygunel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ilkaygunel.constants.ConstantFields;
import com.ilkaygunel.entities.Member;
import com.ilkaygunel.exception.CustomException;
import com.ilkaygunel.pojo.MemberOperationPojo;
import com.ilkaygunel.wrapper.MemberIdWrapp;

@Service
public class MemberDeleteService extends BaseService {

	public MemberOperationPojo deleteOneUserMember(long memberId) {
		MemberOperationPojo memberOperationPojo = deleteOneMember(memberId,
				ConstantFields.ROLE_USER.getConstantField());
		return memberOperationPojo;
	}

	public MemberOperationPojo deleteOneAdminMember(long memberId) {
		MemberOperationPojo memberOperationPojo = deleteOneMember(memberId,
				ConstantFields.ROLE_ADMIN.getConstantField());
		return memberOperationPojo;
	}

	public MemberOperationPojo deleteBulkUserMember(List<MemberIdWrapp> memberIdList) {
		return deleteBulkMember(memberIdList, ConstantFields.ROLE_USER.getConstantField());
	}

	public MemberOperationPojo deleteBulkAdminMember(List<MemberIdWrapp> memberIdList) {
		return deleteBulkMember(memberIdList, ConstantFields.ROLE_ADMIN.getConstantField());
	}

	public MemberOperationPojo deleteOneMember(long memberId, String roleOfMember) {
		List<Member> deletedMemberList = new ArrayList<>();

		MemberOperationPojo memberOperationPojo = new MemberOperationPojo();
		Logger LOGGER = loggingUtil.getLoggerForMemberDeleting(this.getClass());
		try {
			LOGGER.log(Level.INFO, environment.getProperty(roleOfMember + "_memberDeletingMethod"));
			Member memberForDelete = memberUtil.checkMember(memberId, roleOfMember);
			memberRepository.delete(memberId);
			deletedMemberList.add(memberForDelete);
			memberOperationPojo.setMemberList(deletedMemberList);
			memberOperationPojo
					.setResult(environment.getProperty(roleOfMember + "_memberDeletingSuccessfull") + memberForDelete);
			LOGGER.log(Level.INFO,
					environment.getProperty(roleOfMember + "_memberDeletingSuccessfull") + memberForDelete);
		} catch (CustomException e) {
			LOGGER.log(Level.SEVERE, environment.getProperty(roleOfMember + "_memberDeletingFailed") + e.getErrorCode()
					+ " " + e.getErrorMessage());
			memberOperationPojo.setErrorCode(e.getErrorCode());
			memberOperationPojo.setResult(e.getErrorMessage());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, environment.getProperty(roleOfMember + "_memberDeletingFailed") + e.getMessage());
			memberOperationPojo.setResult(e.getMessage());
		}
		return memberOperationPojo;
	}

	private MemberOperationPojo deleteBulkMember(List<MemberIdWrapp> memberIdList, String roleForCheck) {
		MemberOperationPojo deleteMemberOpertaionPojo = memberUtil.checkMemberExistenceOnMemberList(memberIdList,
				roleForCheck);
		MemberOperationPojo memberOperationPojo = new MemberOperationPojo();
		if (ObjectUtils.isEmpty(deleteMemberOpertaionPojo.getErrorCode())) {
			List<Member> deletedMemberList = new ArrayList<>();
			for (MemberIdWrapp memberIdWrapp : memberIdList) {
				MemberOperationPojo temporaryMemberOperationPojo = deleteOneMember(memberIdWrapp.getId(), roleForCheck);
				deletedMemberList.add(temporaryMemberOperationPojo.getMemberList().get(0));
			}
			memberOperationPojo.setResult(resourceBundleMessageManager.getValueOfProperty(
					roleForCheck + "_bulkMemberDeletingSuccessfull", deletedMemberList.get(0).getMemberLanguageCode()));
			memberOperationPojo.setMemberList(deletedMemberList);

		} else {
			memberOperationPojo.setErrorCode(deleteMemberOpertaionPojo.getErrorCode());
			memberOperationPojo.setResult(deleteMemberOpertaionPojo.getResult());
		}
		return memberOperationPojo;
	}

}
