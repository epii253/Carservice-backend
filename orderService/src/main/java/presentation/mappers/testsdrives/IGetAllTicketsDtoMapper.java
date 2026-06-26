package presentation.mappers.testsdrives;

import application.contracts.dataobjects.testdrive.GetAllTickets;
import org.mapstruct.Mapper;
import presentation.entryobjects.testsdrive.GetAllTicketsDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IGetAllTicketsDtoMapper {
    GetAllTickets.Request toRequest(GetAllTicketsDto dto);
}
