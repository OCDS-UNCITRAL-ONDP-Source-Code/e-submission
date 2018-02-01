package com.procurement.submission.service;

import com.procurement.submission.exception.ErrorException;
import com.procurement.submission.model.dto.bpe.ResponseDto;
import com.procurement.submission.model.dto.response.PeriodResponseDto;
import com.procurement.submission.model.entity.PeriodEntity;
import com.procurement.submission.repository.PeriodRepository;
import com.procurement.submission.utils.DateUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class PeriodServiceImpl implements PeriodService {

    private final PeriodRepository periodRepository;
    private final RulesService rulesService;
    private final DateUtil dateUtil;


    public PeriodServiceImpl(final PeriodRepository periodRepository,
                             final RulesService rulesService,
                             final DateUtil dateUtil) {
        this.periodRepository = periodRepository;
        this.rulesService = rulesService;
        this.dateUtil = dateUtil;
    }

    @Override
    public ResponseDto checkInterval(final String country,
                                     final String pmd,
                                     final String stage,
                                     final LocalDateTime startDate,
                                     final LocalDateTime endDate) {
        final int interval = rulesService.getInterval(country, pmd);
        if (!checkInterval(startDate, endDate, interval)) throw new ErrorException("Invalid period.");
        return new ResponseDto<>(true, null, new PeriodResponseDto(startDate, endDate));
    }

    @Override
    public void checkCurrentDateInPeriod(final String cpId, final String stage) {
        if (!isPeriodValid(cpId, stage)) {
            throw new ErrorException("Date does not match the period.");
        }
    }

    @Override
    public void checkIsPeriodExpired(final String cpId, final String stage) {
        if (!isPeriodValid(cpId, stage)) {
            throw new ErrorException("Period has not yet expired.");
        }
    }

    public boolean isPeriodValid(final String cpId, final String stage) {
        final LocalDateTime localDateTime = dateUtil.localNowUTC();
        final PeriodEntity periodEntity = getPeriod(cpId, stage);
        final boolean localDateTimeAfter = localDateTime.isAfter(periodEntity.getStartDate());
        final boolean localDateTimeBefore = localDateTime.isBefore(periodEntity.getEndDate());
        return (localDateTimeAfter && localDateTimeBefore);
    }

    @Override
    public ResponseDto savePeriod(final String cpId,
                                  final String stage,
                                  final LocalDateTime startDate,
                                  final LocalDateTime endDate) {
        final PeriodEntity period = new PeriodEntity();
        period.setCpId(cpId);
        period.setStage(stage);
        period.setStartDate(dateUtil.localToDate(startDate));
        period.setEndDate(dateUtil.localToDate(endDate));
        periodRepository.save(period);
        return new ResponseDto<>(true, null,
                new PeriodResponseDto(
                        period.getStartDate(),
                        period.getEndDate()));
    }

    @Override
    public PeriodEntity getPeriod(final String cpId, final String stage) {
        final Optional<PeriodEntity> entityOptional = periodRepository.getByCpIdAndStage(cpId, stage);
        if (entityOptional.isPresent()) {
            return entityOptional.get();
        } else {
            throw new ErrorException("Period not found");
        }
    }

    private Boolean checkInterval(final LocalDateTime startDate, final LocalDateTime endDate, final int interval) {
        final long days = DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        return days >= interval;
    }
}
