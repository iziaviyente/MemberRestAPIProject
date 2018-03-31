package com.ilkaygunel.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ilkaygunel.entities.MemberRoles;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ObjectUtils;

import com.ilkaygunel.constants.ConstantFields;
import com.ilkaygunel.entities.Member;
import com.ilkaygunel.exception.CustomException;
import com.ilkaygunel.exception.ErrorCodes;
import com.ilkaygunel.pojo.MemberOperationPojo;

@PropertySource(ignoreResourceNotFound = true, value = "classpath:errorMeanings.properties")
@PropertySource(ignoreResourceNotFound = true, value = "classpath:messageTexts.properties")
@Service
public class MemberSaveService extends BaseService{

	public MemberOperationPojo addOneUserMember(Member member) {
		MemberOperationPojo memberOperationPojo = addOneMember(member,ConstantFields.ROLE_USER);
		return memberOperationPojo;

	}

	public MemberOperationPojo addOneAdminMember(Member member) {
		return addOneMember(member,ConstantFields.ROLE_ADMIN);
	}

	public MemberOperationPojo addBulkUserMember(List<Member> memberList) {
		return addBulkMember(memberList,ConstantFields.ROLE_USER);
	}

	public MemberOperationPojo addBulkAdminMember(List<Member> memberList) {
		return addBulkMember(memberList,ConstantFields.ROLE_ADMIN);
	}

	public MemberOperationPojo addOneMember(Member member,String role) {
		MemberOperationPojo memberOperationPojo = new MemberOperationPojo();
		Logger LOGGER = loggingUtil.getLoggerForMemberSaving(this.getClass());
		try {
			LOGGER.log(Level.INFO, environment.getProperty(role + "_memberAddingMethod"));
			checkMemberFields(member);
			member.setPassword(getHashedPassword(member.getPassword()));
			member.setEnabled(true);// In future development, this field will be false and e-mail activation will be
									// required!
			addMemberRolesObject(role,member);
			memberRepository.save(member);
			memberOperationPojo
					.setResult(environment.getProperty(role + "_memberAddingSuccessfull") + member);
			LOGGER.log(Level.INFO, environment.getProperty(role + "_memberAddingSuccessfull") + member);
		} catch (CustomException e) {
			LOGGER.log(Level.SEVERE, environment.getProperty(role + "_memberAddingFaled") + e.getErrorCode()
					+ " " + e.getErrorMessage());
			memberOperationPojo.setErrorCode(e.getErrorCode());
			memberOperationPojo.setResult(e.getErrorMessage());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, environment.getProperty(role + "_memberAddingFaled") + e.getMessage());
			memberOperationPojo.setResult(e.getMessage());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return memberOperationPojo;
	}

	public MemberOperationPojo addBulkMember(List<Member> memberList,String role) {
		MemberOperationPojo memberOperationPojo = new MemberOperationPojo();
		Logger LOGGER = loggingUtil.getLoggerForMemberSaving(this.getClass());
		try {
			LOGGER.log(Level.INFO, environment.getProperty(role + "_bulkMemberAddingMethod"));
			for (Member member : memberList) {
				checkMemberFields(member);
				member.setPassword(getHashedPassword(member.getPassword()));
				member.setEnabled(true);// In future development, this field will be false and e-mail activation will be
				// required!
				addMemberRolesObject(role,member);
				memberRepository.save(member);
			}
			memberOperationPojo.setResult(
					environment.getProperty(role + "_bulkMemberAddingSuccessfull") + memberList);
			LOGGER.log(Level.INFO,
					environment.getProperty(role + "_bulkMemberAddingSuccessfull") + memberList);
		} catch (CustomException e) {
			LOGGER.log(Level.SEVERE, environment.getProperty(role + "_bulkMemberAddingFaled")
					+ e.getErrorCode() + " " + e.getErrorMessage());
			memberOperationPojo.setErrorCode(e.getErrorCode());
			memberOperationPojo.setResult(e.getErrorMessage());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					environment.getProperty(role + "_bulkMemberAddingFaled") + e.getMessage());
			memberOperationPojo.setResult(e.getMessage());
		}
		return memberOperationPojo;
	}

	public void checkMemberFields(Member member) throws CustomException {
		if (ObjectUtils.isEmpty(member.getEmail())) {
			throw new CustomException(ErrorCodes.ERROR_05, environment.getProperty(ErrorCodes.ERROR_05));
		} else if (memberRepository.findByEmail(member.getEmail()) != null) {
			throw new CustomException(ErrorCodes.ERROR_06, environment.getProperty(ErrorCodes.ERROR_06));
		}
	}

	private String getHashedPassword(String rawPassword) {
		return new BCryptPasswordEncoder().encode(rawPassword);
	}

	private void addMemberRolesObject(String role,Member member){
		MemberRoles rolesOfMember = new MemberRoles();
		rolesOfMember.setRole(role);
		rolesOfMember.setEmail(member.getEmail());
		member.getRolesOfMember().add(rolesOfMember);
	}
}
