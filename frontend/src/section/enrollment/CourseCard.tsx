import {Button, Card, CardActions, CardContent, Typography} from "@mui/material";

interface CourseCardProps {
    courseCode: string;
    courseName: string;
    courseDescription?: string;
    credits: number;
    sectionsCount: number;
    warning: string | null;
    onEnrollClick: () => void;
}

export default ({ courseCode, courseName, courseDescription, credits, sectionsCount, warning, onEnrollClick }: CourseCardProps) => {
    return (
        <Card variant="outlined">
            <CardContent>
                <Typography variant="h6" component="div">
                    {courseCode}
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                    {courseName}
                </Typography>
                {courseDescription && (
                    <Typography variant="body2" sx={{ mt: 1, mb: 1 }}>
                        {courseDescription}
                    </Typography>
                )}
                <Typography variant="body2">
                    <strong>Credits:</strong> {credits}
                </Typography>
                <Typography variant="body2">
                    <strong>Sections available:</strong> {sectionsCount}
                </Typography>
                {warning &&
                    <Typography variant="body2" component="p" color="error">
                        {warning}
                    </Typography>
                }
            </CardContent>
            <CardActions>
                <Button
                    size="small"
                    variant="contained"
                    color="primary"
                    onClick={onEnrollClick}
                >
                    Enroll
                </Button>
            </CardActions>
        </Card>
    );
}
