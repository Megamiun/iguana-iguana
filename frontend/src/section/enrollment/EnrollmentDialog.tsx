import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    List,
    ListItem,
    ListItemText,
    Typography
} from "@mui/material";
import {AvailableCourseSectionResponse} from "../../types/schedule";
import {getUnavailabilityMessage} from "./utils";

interface EnrollmentDialogProps {
    open: boolean;
    courseCode?: string;
    courseName?: string;
    courseDescription?: string;
    sections?: AvailableCourseSectionResponse[];
    onClose: () => void;
    onEnroll: (sectionId: number) => void;
}

export default ({ open, courseCode, courseName, courseDescription, sections, onClose, onEnroll }: EnrollmentDialogProps) => {
    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="md"
            fullWidth
        >
            <DialogTitle>
                {courseCode} - {courseName}
            </DialogTitle>
            <DialogContent>
                {courseDescription && (
                    <Typography variant="body2" component="p">
                        {courseDescription}
                    </Typography>
                )}
                <Typography variant="subtitle2" gutterBottom>
                    Select a section to enroll:
                </Typography>
                <List>
                    {sections?.map((section) => {
                        const isAvailable = section.available;
                        return (
                            <ListItem
                                key={section.sectionId}
                                secondaryAction={
                                    <Button
                                        variant="contained"
                                        size="small"
                                        onClick={() => onEnroll(section.sectionId)}
                                        disabled={!isAvailable}
                                    >
                                        Enroll
                                    </Button>
                                }
                            >
                                <ListItemText
                                    primary={`Section ${section.section} - ${section.teacher}`}
                                    secondary={
                                        <>
                                            <Typography variant="body2" component="p">
                                                <strong>Classroom:</strong> {section.classroom}
                                            </Typography>
                                            <Typography variant="body2" component="p">
                                                <strong>Spots:</strong> {section.availableSpots} available / {section.filledSpots} filled
                                            </Typography>
                                            <Typography variant="body2" component="p">
                                                <strong>Schedule:</strong>{' '}
                                                {section.schedule.map(slot =>
                                                    `${slot.weekday} ${slot.start}:00-${slot.end}:00`
                                                ).join(', ')}
                                            </Typography>
                                            {!isAvailable && section.unavailableReason && (
                                                <Typography variant="body2" component="p" color="error">
                                                    {getUnavailabilityMessage(section.unavailableReason)}
                                                </Typography>
                                            )}
                                        </>
                                    }
                                />
                            </ListItem>
                        );
                    })}
                </List>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    );
}
