#! /usr/bin/python3
import sys

out = ''
with open(sys.argv[1], 'r') as input_file:
    for line in input_file:
        line = line.rstrip()
        if line.startswith('v '):
            coords = [float(c) for c in line[2:].split(' ')]
            line = 'v '
            line += str(coords[0] / 16 + 0.5) + ' '
            line += str(coords[1] / 16) + ' '
            line += str(coords[2] / 16 + 0.5) + ' '
            line += '\n'
        elif line.startswith('f '):
            face_vertices = line[2:].split(' ')
            if len(face_vertices) % 2 == 1:
                face_vertices.append(face_vertices[-1])
            split_faces = []
            for i in range(1, len(face_vertices) - 2, 2):
                split_faces.append([face_vertices[0], face_vertices[i], face_vertices[i + 1], face_vertices[i + 2]])
            line = ''
            for face in split_faces:
                line += 'f ' + ' '.join(face) + '\n'
        else:
            line += '\n'
        out += line

with open(sys.argv[2], 'w') as output:
    output.write(out)
