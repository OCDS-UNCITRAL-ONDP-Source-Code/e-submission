package com.procurement.submission.service;

import com.procurement.submission.exception.ErrorException;
import com.procurement.submission.model.dto.request.PeriodDataDto;
import com.procurement.submission.model.dto.request.TenderPeriodDto;
import com.procurement.submission.model.entity.SubmissionPeriodEntity;
import com.procurement.submission.repository.PeriodRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class PeriodServiceImpl implements PeriodService {

    private PeriodRepository periodRepository;
    private RulesService rulesService;
    private ConversionService conversionService;

    public PeriodServiceImpl(final PeriodRepository periodRepository,
                             final RulesService rulesService,
                             final ConversionService conversionService) {
        this.periodRepository = periodRepository;
        this.rulesService = rulesService;
        this.conversionService = conversionService;
    }

    @Override
    public Boolean checkPeriod(final PeriodDataDto dataDto) {
        final long interval = rulesService.getInterval(dataDto);
        Boolean isValid = false;
        if (interval != 0L) {
            final TenderPeriodDto tenderPeriod = dataDto.getTenderPeriod();
            isValid = checkInterval(tenderPeriod.getStartDate(),
                tenderPeriod.getEndDate(),
                interval);
        }
        return isValid;
    }

    @Override
    public void checkPeriod(final String ocid) {
        final SubmissionPeriodEntity periodEntity =
            Optional.ofNullable(periodRepository.getByOcId(ocid))
                    .orElseThrow(() -> new ErrorException("No period in DB"));
        final LocalDateTime localDateTime = LocalDateTime.now();
        final boolean localDateTimeAfter = localDateTime.isAfter(periodEntity.getStartDate());
        final boolean localDateTimeBefore = localDateTime.isBefore(periodEntity.getEndDate());
        if (!localDateTimeAfter || !localDateTimeBefore) {
            throw new ErrorException("Not found date.");
        }
    }

    private Boolean checkInterval(final LocalDateTime startDate, final LocalDateTime endDate, final Long interval) {
        final long days = DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        return days >= interval;
    }

    @Override
    public SubmissionPeriodEntity savePeriod(final PeriodDataDto dataDto) {
        final SubmissionPeriodEntity period = conversionService.convert(dataDto, SubmissionPeriodEntity.class);
        return periodRepository.save(period);
    }
}
